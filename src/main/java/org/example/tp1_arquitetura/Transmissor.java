package org.example.tp1_arquitetura;

import java.io.File;
import java.util.Arrays;

public class Transmissor {
    private String mensagem;
    private Canal canal;
    private File arquivo;
    private Estrategia tecnica;

    public Transmissor(String mensagem, Canal canal, Estrategia tecnica) {
        this.mensagem = mensagem;
        this.canal = canal;
        this.tecnica = tecnica;
    }

    public Transmissor(File arq, Canal canal, Estrategia tecnica) {
        this.arquivo = arq;
        this.canal = canal;
        this.tecnica = tecnica;

        carregarMensagemArquivo();
    }

    private void carregarMensagemArquivo() {
        /*sua implementação aqui!!!
        modifique o que precisar neste método para carregar na mensagem 
        todo o conteúdo do arquivo
        */
    }

    //convertendo um símbolo para "vetor" de boolean (bits)
    private boolean[] streamCaracter(char simbolo) {

        //cada símbolo da tabela ASCII é representado com 8 bits
        boolean bits[] = new boolean[8];

        //convertendo um char para int (encontramos o valor do mesmo na tabela ASCII)
        int valorSimbolo = (int) simbolo;
        int indice = 7;

        //convertendo cada "bits" do valor da tabela ASCII
        while (valorSimbolo >= 2) {
            int resto = valorSimbolo % 2;
            valorSimbolo /= 2;
            bits[indice] = (resto == 1);
            indice--;
        }
        bits[indice] = (valorSimbolo == 1);

        return bits;
    }

    /*

    1 0 1 0 1 0 1 0 0 1 | 1 0 0 1 1
    1 0 0 1 1 | |
    --------- ↓ ↓
    0 0 1 1 0 0 1
        1 0 0 1 1
        ---------
        0 1 0 1 0

    */

    private boolean[] dadoBitsCRC(boolean bits[]) {

        boolean resto[] = Arrays.copyOf(bits, 5); // Copia os primeiros 5 itens de "bits"

        for (int i = 5; i < bits.length; ) {
            // Faz o XOR dos 5 elementos atuais do resto com os 5 elementos do polinômio
            for (int j = 0; j < 5; j++) {
                if (resto[j] == Canal.polinomio[j]) {
                    resto[j] = false;
                } else resto[j] = true;
            }
            // Retira os 0s a esquerda do resto e pega os próximos números de bits, quando há
            for (int j = 0; j < 5; j++) {
                if (!resto[j]) {
                    resto[0] = resto[1];
                    resto[1] = resto[2];
                    resto[2] = resto[3];
                    resto[3] = resto[4];
                    resto[4] = bits[i];
                    i++;
                    if(i < bits.length){
                        break;
                    }
                } else break;
            }
        }

        // Novo array com os bits completos
        boolean[] bitsCompletos = new boolean[bits.length + Canal.polinomio.length];

        // Copia os elementos originais para um novo array
        System.arraycopy(bits, 0, bitsCompletos, 0, bits.length);

        // Copia os 5 elementos do resto ao final do novo array
        System.arraycopy(resto, 0, bitsCompletos, bits.length, Canal.polinomio.length);

        return bitsCompletos;

        /*for (int i = 4; i < bits.length; ) {
            for (int j = 0; j < 5; j++) {
                if (bits[i - 4 - j] == Canal.polinomio[j]) {
                    resto[j] = false;
                } else resto[j] = true;
            }
            for (int j = 0; j < 5; j++) {
                if(!resto[j]){
                    i++;
                } else break;
            }
        }*/
    }

    private boolean[] dadoBitsHamming(boolean bits[]) {

        return bits;
    }

    public void enviaDado() {
        for (int i = 0; i < this.mensagem.length(); i++) {
            do {
                boolean bits[] = streamCaracter(this.mensagem.charAt(i));

                for(boolean b : bits){
                    System.out.print((b ? "1" : "0") + ", ");
                }
                System.out.println();

                // Adicionando bits de verificação
                boolean bitsCompletos[] = this.tecnica == Estrategia.CRC ? dadoBitsCRC(bits) : dadoBitsHamming(bits);

                // Enviando cada caractere em forma de vetor de bits pela rede
                this.canal.enviarDado(bitsCompletos);
            } while (this.canal.recebeFeedback() == false);
            // Pedindo o reenvio da mensagem, devido a ocorrência de um erro que não pode ser coeeigido
        }
    }
}
