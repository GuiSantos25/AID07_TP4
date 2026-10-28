package sensores;

import registo.Leitura;
import registo.RegistoCentral;
import java.util.Random;

public class Sensor implements Runnable {
    private final String nome;
    private final String unidade;
    private final double minValor;
    private final double maxValor;
    private final long intervaloBaseMs;
    private final RegistoCentral registoCentral;
    
    private volatile boolean ativo = true;
    private int leiturasEfetuadas = 0;
    private final Random random = new Random();

    public Sensor(String nome, String unidade, double minValor, double maxValor, 
                  long intervaloBaseMs, RegistoCentral registoCentral) {
        this.nome = nome;
        this.unidade = unidade;
        this.minValor = minValor;
        this.maxValor = maxValor;
        this.intervaloBaseMs = intervaloBaseMs;
        this.registoCentral = registoCentral;
    }

    public void parar() {
        this.ativo = false;
    }

    public int getLeiturasEfetuadas() {
        return leiturasEfetuadas;
    }

    @Override
    public void run() {
        System.out.println("[" + nome + "] Iniciado.");
        while (ativo && !Thread.currentThread().isInterrupted()) {
            try {
                double valor = minValor + (maxValor - minValor) * random.nextDouble();
                Leitura leitura = new Leitura(nome, valor, unidade);
                
                // Registo atómico no recurso partilhado
                registoCentral.registar(leitura);
                leiturasEfetuadas++;
                
                System.out.println(leitura);

                // Cálculo do intervalo efetivo com jitter de ±20%
                double jitter = intervaloBaseMs * 0.20;
                long variacao = (long) ((random.nextDouble() * 2 * jitter) - jitter);
                long intervaloEfetivo = Math.max(10, intervaloBaseMs + variacao);

                // Thread.sleep mantido estritamente fora de blocos synchronized
                Thread.sleep(intervaloEfetivo);
            } catch (InterruptedException e) {
                // Tratamento rigoroso da InterruptedException: restaurar o estado e terminar
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[" + nome + "] terminado de forma controlada (" + leiturasEfetuadas + " leituras).");
    }
}