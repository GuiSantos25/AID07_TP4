package registo;

import java.util.*;

public class RegistoCentral {
    private final List<Leitura> historico = new ArrayList<>();
    private int contadorTotal = 0;
    private final Map<String, EstatisticasSensor> estatisticasPorSensor = new HashMap<>();

    public static class EstatisticasSensor {
        private int contagem = 0;
        private double minimo = Double.MAX_VALUE;
        private double maximo = Double.MIN_VALUE;
        private double soma = 0.0;
        private final String unidade;

        public EstatisticasSensor(String unidade) {
            this.unidade = unidade;
        }

        public void atualizar(double valor) {
            contagem++;
            soma += valor;
            if (valor < minimo) minimo = valor;
            if (valor > maximo) maximo = valor;
        }

        public int getContagem() { return contagem; }
        public double getMinimo() { return contagem == 0 ? 0.0 : minimo; }
        public double getMaximo() { return contagem == 0 ? 0.0 : maximo; }
        public double getMedia() { return contagem == 0 ? 0.0 : soma / contagem; }
        public String getUnidade() { return unidade; }
    }

    public synchronized void registar(Leitura leitura) {
        historico.add(leitura);
        contadorTotal++;
        estatisticasPorSensor.computeIfAbsent(leitura.getSensorNome(), 
            k -> new EstatisticasSensor(leitura.getUnidade())).atualizar(leitura.getValor());
    }

    public synchronized List<Leitura> getUltimasLeituras() {
        Map<String, Leitura> ultimas = new LinkedHashMap<>();
        for (Leitura l : historico) {
            ultimas.put(l.getSensorNome(), l);
        }
        return new ArrayList<>(ultimas.values());
    }

    public synchronized Map<String, EstatisticasSensor> getEstatisticas() {
        Map<String, EstatisticasSensor> copia = new HashMap<>();
        for (Map.Entry<String, EstatisticasSensor> entry : estatisticasPorSensor.entrySet()) {
            EstatisticasSensor orig = entry.getValue();
            EstatisticasSensor est = new EstatisticasSensor(orig.unidade);
            est.contagem = orig.contagem;
            est.minimo = orig.minimo;
            est.maximo = orig.maximo;
            est.soma = orig.soma;
            copia.put(entry.getKey(), est);
        }
        return copia;
    }

    public synchronized int getContadorTotal() {
        return contadorTotal;
    }

    public synchronized int getTamanhoHistorico() {
        return historico.size();
    }

    public synchronized int getSomaContagensSensores() {
        int soma = 0;
        for (EstatisticasSensor est : estatisticasPorSensor.values()) {
            soma += est.getContagem();
        }
        return soma;
    }

    public synchronized boolean verificarConsistencia() {
        int somaSensores = getSomaContagensSensores();
        return (contadorTotal == somaSensores) && (contadorTotal == historico.size());
    }
}