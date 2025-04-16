import org.example.tp1_arquitetura.Canal;
import org.example.tp1_arquitetura.Estrategia;
import org.example.tp1_arquitetura.Receptor;
import org.example.tp1_arquitetura.Transmissor;

public class TP1_Arquitetura {

    public static void main(String[] args) {

        Canal canal = new Canal(0.1, 0.0);
        
        Transmissor transm = new Transmissor("Teste:?*/", canal, Estrategia.CRC);
        Receptor receber = new Receptor(canal, Estrategia.CRC);
        
        canal.conectaTransmissor(transm);
        canal.conectaReceptor(receber);
        
        // Mensurando o tempo de execução
        long tempoI = System.currentTimeMillis();
        transm.enviaDado();
        long tempoF = System.currentTimeMillis();
        
        System.out.println("Tempo total: " + (tempoF - tempoI));
        
        System.out.println(receber.getMensagem());

        Transmissor transmH = new Transmissor("Teste:?*/", canal, Estrategia.CRC);
        Receptor receberH = new Receptor(canal, Estrategia.HAMMING);

        canal.conectaTransmissor(transmH);
        canal.conectaReceptor(receberH);

        // Mensurando o tempo de execução
        tempoI = System.currentTimeMillis();
        transmH.enviaDado();
        tempoF = System.currentTimeMillis();

        System.out.println("Tempo total: " + (tempoF - tempoI));

        System.out.println(receberH.getMensagem());
    }
}
