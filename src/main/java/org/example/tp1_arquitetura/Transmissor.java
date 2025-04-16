package org.example.tp1_arquitetura;

import java.io.File;

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

    private boolean[] dadoBitsCRC(boolean bits[]) {

        return bits;
    }

    private boolean[] dadoBitsHamming(boolean bits[]) {

        return bits;
    }

    public void enviaDado() {
        for (int i = 0; i < this.mensagem.length(); i++) {
            do {
                boolean bits[] = streamCaracter(this.mensagem.charAt(i));

                // Adicionando bits de verificação
                boolean bitsCompĺetos[] = this.tecnica == Estrategia.CRC ? dadoBitsCRC(bits) : dadoBitsHamming(bits);

                // Enviando cada caractere em forma de vetor de bits pela rede
                this.canal.enviarDado(bits);
            } while (this.canal.recebeFeedback() == false);
            // Pedindo o reenvio da mensagem, devido a ocorrência de um erro que não pode ser coeeigido
        }
    }
}
