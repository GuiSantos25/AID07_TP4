package registo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Leitura {
    private final String sensorNome;
    private final double valor;
    private final String unidade;
    private final LocalDateTime instante;

    public Leitura(String sensorNome, double valor, String unidade) {
        this.sensorNome = sensorNome;
        this.valor = valor;
        this.unidade = unidade;
        this.instante = LocalDateTime.now();
    }

    public String getSensorNome() {
        return sensorNome;
    }

    public double getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public LocalDateTime getInstante() {
        return instante;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return String.format("[%s] %s: %.1f %s", instante.format(formatter), sensorNome, valor, unidade);
    }
}