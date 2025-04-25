import org.example.tp1_arquitetura.Canal;
import org.example.tp1_arquitetura.Estrategia;
import org.example.tp1_arquitetura.Receptor;
import org.example.tp1_arquitetura.Transmissor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Scanner;

public class TP1_Arquitetura {

    /*public static void main(String[] args) {

        Canal canal = new Canal(0.1);

        //abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ


        String mensagem = "";
        try {
            mensagem = new String(Files.readAllBytes(Paths.get("/home/anybody/Documents/Projects/TP1_Arquitetura/src/main/java/Moby Dick.txt")));
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return;
        }

        Transmissor transm = new Transmissor("", canal, Estrategia.HAMMING);
        Receptor receber = new Receptor(canal, Estrategia.HAMMING);

        canal.conectaTransmissor(transm);
        canal.conectaReceptor(receber);

        // Mensurando o tempo de execução
        long tempoI = System.currentTimeMillis();
        transm.enviaDado();
        long tempoF = System.currentTimeMillis();

        System.out.println("Tempo total: " + (tempoF - tempoI));

        receber.gravaMensArquivo();

    }*/

    public static void main(String[] args) {
        // Parâmetros de ruído a serem testados
        double[] ruidos = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
        Estrategia[] tecnicas = {Estrategia.CRC, Estrategia.HAMMING};

        // Caminho do arquivo de entrada
        String caminhoArquivo = "C:\\Users\\0063398\\Documents\\Projetos\\TP1_Arquitetura\\src\\main\\java\\Moby Dick.txt";
        File arquivo = new File(caminhoArquivo);

        // Monta o cabeçalho do CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Técnica");
        for (double ruido : ruidos) {
            csv.append(String.format(Locale.US, ",%.2f", (ruido * 100))).append("%");
        }
        csv.append("\n");

        // Executa os testes para cada técnica
        for (Estrategia tecnica : tecnicas) {
            csv.append(tecnica.name());
            for (double ruido : ruidos) {
                Canal canal = new Canal(ruido);
                Transmissor transm = new Transmissor(arquivo, canal, tecnica);
                Receptor receber = new Receptor(canal, tecnica);

                canal.conectaTransmissor(transm);
                canal.conectaReceptor(receber);

                long tempoI = System.currentTimeMillis();
                transm.enviaDado();
                long tempoF = System.currentTimeMillis();

                long tempoExecucao = tempoF - tempoI;
                System.out.println(tempoExecucao);
                csv.append(",").append(tempoExecucao);
            }
            csv.append("\n");
        }

        // Salva o CSV
        String nomeCsv = "resultados_tempo_execucao.csv";
        try {
            Files.write(
                Paths.get(nomeCsv),
                csv.toString().getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
            System.out.println("Resultados salvos em: " + nomeCsv);
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
        }
    }
}
