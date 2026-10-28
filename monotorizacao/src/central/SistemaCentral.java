package central;

import registo.RegistoCentral;
import registo.Leitura;
import sensores.Sensor;

import java.util.*;

public class SistemaCentral {
    private final RegistoCentral registoCentral = new RegistoCentral();
    private final List<Sensor> sensores = new ArrayList<>();
    private final List<Thread> threadsSensores = new ArrayList<>();

    public void iniciar() {
        configurarSensores();

        long inicioMonitorizacao = System.currentTimeMillis();

        for (int i = 0; i < sensores.size(); i++) {
            Sensor sensor = sensores.get(i);
            Thread t = new Thread(sensor, "Sensor-" + sensor.toString().substring(0, 0) + getNomeSensor(i));
            // Nome explícito para a thread
            Thread threadSensor = new Thread(sensor, "Sensor-" + getNomeSensor(i));
            threadsSensores.add(threadSensor);
            threadSensor.start(); // Arranque obrigatório com start()
        }

        Scanner scanner = new Scanner(System.in);
        boolean executando = true;

        while (executando) {
            exibirMenu();
            if (!scanner.hasNextLine()) {
                break; // Tratamento de EOF
            }
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    mostrarUltimasLeituras();
                    break;
                case "2":
                    mostrarEstatisticasParciais();
                    break;
                case "0":
                    executando = false;
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
                    break;
            }
        }

        // --- Graceful Shutdown ---
        System.out.println("\nEncerramento coordenado iniciado...");
        long inicioEncerramento = System.currentTimeMillis();

        // 1. Sinalizar paragem cooperativa
        for (int i = 0; i < sensores.size(); i++) {
            sensores.get(i).parar();
            threadsSensores.get(i).interrupt();
        }

        // 2. Aguardar terminação com join()
        for (Thread t : threadsSensores) {
            try {
                t.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long fimEncerramento = System.currentTimeMillis();
        long duracaoEncerramentoMs = fimEncerramento - inicioEncerramento;
        long duracaoMonitorizacaoMs = fimEncerramento - inicioMonitorizacao;

        // 3. Relatório final sobre estado consistente
        emitirRelatorioFinal(duracaoMonitorizacaoMs, duracaoEncerramentoMs);
    }

    private String getNomeSensor(int index) {
        if (index == 0) return "TEMP-01";
        if (index == 1) return "HUM-01";
        if (index == 2) return "PRES-01";
        return "SENSO-" + (index + 1);
    }

    private void configurarSensores() {
        sensores.add(new Sensor("TEMP-01", "°C", 18.0, 27.0, 500, registoCentral));
        sensores.add(new Sensor("HUM-01", "%", 40.0, 60.0, 800, registoCentral));
        sensores.add(new Sensor("PRES-01", "hPa", 1000.0, 1025.0, 1000, registoCentral));
    }

    private void exibirMenu() {
        System.out.println("\n--- CENTRAL DE MONITORIZAÇÃO AMBIENTAL ---");
        System.out.println("[1] Últimas leituras");
        System.out.println("[2] Estatísticas parciais");
        System.out.println("[0] Encerrar");
        System.out.print("Escolha uma opção: ");
    }

    private void mostrarUltimasLeituras() {
        System.out.println("\n--- ÚLTIMAS LEITURAS ---");
        List<Leitura> ultimas = registoCentral.getUltimasLeituras();
        if (ultimas.isEmpty()) {
            System.out.println("Sem leituras registadas.");
        } else {
            for (Leitura l : ultimas) {
                System.out.println(l);
            }
        }
    }

    private void mostrarEstatisticasParciais() {
        System.out.println("\n--- ESTATÍSTICAS PARCIAIS ---");
        Map<String, RegistoCentral.EstatisticasSensor> stats = registoCentral.getEstatisticas();
        if (stats.isEmpty()) {
            System.out.println("Sem estatísticas disponíveis.");
            return;
        }
        System.out.printf("%-10s | %-8s | %-8s | %-8s | %-8s | %-8s\n", "Sensor", "Leituras", "Mínimo", "Máximo", "Média", "Unidade");
        System.out.println("---------------------------------------------------------------");
        for (Map.Entry<String, RegistoCentral.EstatisticasSensor> entry : stats.entrySet()) {
            String nome = entry.getKey();
            RegistoCentral.EstatisticasSensor s = entry.getValue();
            System.out.printf("%-10s | %8d | %8.1f | %8.1f | %8.1f | %-8s\n", 
                    nome, s.getContagem(), s.getMinimo(), s.getMaximo(), s.getMedia(), s.getUnidade());
        }
    }

    private void emitirRelatorioFinal(long duracaoMonitorizacaoMs, long duracaoEncerramentoMs) {
        Map<String, RegistoCentral.EstatisticasSensor> stats = registoCentral.getEstatisticas();
        int contadorTotal = registoCentral.getContadorTotal();
        int somaContagens = registoCentral.getSomaContagensSensores();
        int historicoSize = registoCentral.getTamanhoHistorico();
        boolean consistente = registoCentral.verificarConsistencia();

        System.out.println("================================================================");
        System.out.println("RELATÓRIO FINAL DE MONITORIZAÇÃO");
        System.out.println("================================================================");
        System.out.printf("Duração da monitorização : %.1f s\n", duracaoMonitorizacaoMs / 1000.0);
        System.out.printf("Duração do encerramento : %d ms\n", duracaoEncerramentoMs);
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-10s %8s %8s %8s %8s %-8s\n", "Sensor", "Leituras", "Mínimo", "Máximo", "Média", "Unidade");
        
        List<String> nomesSensores = new ArrayList<>(stats.keySet());
        Collections.sort(nomesSensores);
        for (String nome : nomesSensores) {
            RegistoCentral.EstatisticasSensor s = stats.get(nome);
            System.out.printf("%-10s %8d %8.1f %8.1f %8.1f %-8s\n", 
                    nome, s.getContagem(), s.getMinimo(), s.getMaximo(), s.getMedia(), s.getUnidade());
        }

        System.out.println("----------------------------------------------------------------");
        System.out.printf("Total de leituras (contador) : %d\n", contadorTotal);
        System.out.printf("Soma das leituras por sensor : %d\n", somaContagens);
        System.out.printf("Registos no histórico : %d\n", historicoSize);
        System.out.printf("Verificação de consistência : %s\n", consistente ? "OK" : "FALHA");
        System.out.println("Estado final das threads :");
        
        boolean todasTerminadas = true;
        for (Thread t : threadsSensores) {
            boolean terminada = !t.isAlive();
            if (!terminada) todasTerminadas = false;
            System.out.printf("  %-15s %s\n", t.getName(), t.getState());
        }
        System.out.printf("Todas as threads terminadas : %s\n", todasTerminadas ? "SIM" : "NÃO");
        System.out.println("================================================================");
    }
}