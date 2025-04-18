package org.example.tp1_arquitetura;

import java.util.Arrays;

public class Receptor {

    // Mensagem recebida pelo transmissor
    private String mensagem;
    private final Estrategia tecnica;
    private final Canal canal;

    // Atributo que indica a integridade do dado
    private boolean estaIntegro;

    public Receptor(Canal canal, Estrategia tecnica) {
        // Mensagem vazia no inicio da execução
        this.mensagem = "";
        this.tecnica = tecnica;
        this.canal = canal;
        this.estaIntegro = true;
    }

    public String getMensagem() {
        return mensagem;
    }

    public static boolean[] adicionaZerosAEsquerda(boolean[] bits, int tamanho) {
        if (bits.length >= tamanho) {
            return bits;
        }
        boolean[] resultado = new boolean[tamanho];
        int offset = tamanho - bits.length;
        // Os primeiros 'offset' elementos já são false por padrão
        System.arraycopy(bits, 0, resultado, offset, bits.length);
        return resultado;
    }

    private void decodificarDado(boolean[] bits) {
        int codigoAscii = 0;
        int expoente = bits.length - 1;

        // Converntendo os "bits" para valor inteiro para então encontrar o valor da tabela ASCII
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                codigoAscii += Math.pow(2, expoente);
            }
            expoente--;
        }

        /*System.out.println((char) codigoAscii);*/

        // Concatenando cada simbolo na mensagem original
        this.mensagem += (char) codigoAscii;
    }

    private boolean[] verificaDadoCRC(boolean[] bits) {

        /*System.out.println("Bits originais receptor: ");
        Canal.printBits(bits);*/

        boolean[] resto = Arrays.copyOf(bits, 5); // Copia os primeiros 5 itens de "bits"

        for (int i = 5; i < bits.length; ) {
            // Faz o XOR dos 5 elementos atuais do resto com os 5 elementos do polinômio
            for (int j = 0; j < 5; j++) {
                resto[j] = resto[j] != Canal.polinomio[j];
            }

            // Retira os 0s a esquerda do resto e pega os próximos números de bitsVerificacao, quando há
            for (int j = 0; j < 5; j++) {
                if (!resto[j]) {
                    resto[0] = resto[1];
                    resto[1] = resto[2];
                    resto[2] = resto[3];
                    resto[3] = resto[4];
                    resto[4] = bits[i];
                    i++;
                    if (i == bits.length && resto[0]) {
                        for (int c = 0; c < 5; c++) {
                            resto[c] = resto[c] != Canal.polinomio[c];
                        }
                        break;
                    }
                    j = -1;
                    if (i >= bits.length) {
                        break;
                    }
                } else break;
            }
        }

        this.estaIntegro = true;

        for (boolean b : resto) {
            if (b) {
                this.estaIntegro = false;
                break;
            }
        }

        boolean[] bitsOriginais = new boolean[bits.length - (Canal.polinomio.length - 1)];

        // Copia os elementos originais para um novo array
        System.arraycopy(bits, 0, bitsOriginais, 0, bitsOriginais.length);

        /*System.out.println("Bits originais: ");
        Canal.printBits(bitsOriginais);

        System.out.println("Bits sem zeros adcionais: ");
        Canal.printBits(adicionaZerosAEsquerda(bitsOriginais, 8));*/

        return adicionaZerosAEsquerda(bitsOriginais, 8);
    }

    private boolean[] verificaDadoHammig(boolean[] bits) {
        this.estaIntegro = true;
        return bits;
    }


    //recebe os dados do transmissor
    public void receberDadoBits() {
        boolean bitsVerificados[] = this.tecnica == Estrategia.CRC ? verificaDadoCRC(this.canal.recebeDado()) : verificaDadoHammig(this.canal.recebeDado());

        if (this.estaIntegro) decodificarDado(bitsVerificados);

        // Retorna verdadeiro ou falso conforme a integridade do dado definida nos métodos de verificação
        this.canal.enviaFeedBack(this.estaIntegro);
    }

    public void gravaMensArquivo() {
        /*
        aqui você deve implementar um mecanismo para gravar a mensagem em arquivo
        */
    }
}
