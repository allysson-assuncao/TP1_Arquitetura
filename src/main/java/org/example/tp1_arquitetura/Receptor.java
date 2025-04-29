package org.example.tp1_arquitetura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        System.out.print((char) codigoAscii);

        // Concatenando cada simbolo na mensagem original
        this.mensagem += (char) codigoAscii;
    }

    // Verifica a integridade do dado recebido na rede utilizando a técnica CRC
    private boolean[] verificaDadoCRC(boolean[] bits) {

        // Começa igual ao do transmissor
        boolean[] resto = Arrays.copyOf(bits, 5);

        for (int i = 5; i < bits.length; ) {
            for (int j = 0; j < 5; j++) {
                resto[j] = resto[j] != Canal.polinomio[j];
            }

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

        // Caso o resto tenha algum 1, o dado não está integro
        for (boolean b : resto) {
            if (b) {
                this.estaIntegro = false;
                break;
            }
        }

        // Novo vetor sem os bits de verificação adicionais
        boolean[] bitsOriginais = new boolean[bits.length - (Canal.polinomio.length - 1)];

        // Copia os elementos originais para um novo array
        System.arraycopy(bits, 0, bitsOriginais, 0, bitsOriginais.length);

        return Canal.adicionaZerosAEsquerda(bitsOriginais, 8);
    }

    // Verifica a integridade do dado recebido na rede utilizando a técnica Hamming
    private boolean[] verificaDadoHammig(boolean[] bits) {

        /*Canal.printBits(bits);*/

        this.estaIntegro = true;

        int quantBitsHamming = Canal.calcularBitsParidade(bits.length);

        // Além da implementação do transmissor, soma as posições dos 1s encontrados para possível correção
        int quantidade1s = 0;
        boolean bitHammingPar;
        int indiceCorrecao = 0;
        for (int i = 1; i <= bits.length; i++) {
            if (Canal.isPotenciaDeDois(i)) {
                int indice1 = Canal.ondeEstaO1(Canal.intToBits(i));
                for (int j = i; j <= bits.length; j++) {
                    if (Canal.intToBits(j)[indice1] && bits[j - 1]) quantidade1s++;
                }
                bitHammingPar = (quantidade1s % 2 == 0);

                // Caso seja 1, não está integro
                this.estaIntegro = bitHammingPar;
                quantidade1s = 0;

                // Caso o bit seja 1, soma a posição
                if (!bitHammingPar) indiceCorrecao += i;
            }
        }

        // Caso o indice da correção seja valido, inverte o bit e garante a integridade do dado
        if (indiceCorrecao > bits.length) {
            this.estaIntegro = false;
        } else if (indiceCorrecao != 0) {
            bits[indiceCorrecao - 1] = !bits[indiceCorrecao - 1];
            this.estaIntegro = true;
        }

        // Novo array com os bits recebidos menos os bits de hamming que foram verificados
        boolean[] bitsOriginais = new boolean[bits.length - 4];

        // Adiciona os elementos no vetor, pulando os bits de verificação
        int c = 0;
        for (int i = 0; i < bits.length; i++) {
            if (Canal.isPotenciaDeDois(i + 1)) {
                continue;
            }
            try {
                bitsOriginais[c] = bits[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Erro ao acessar o indice do vetor: " + e.getMessage());
                Canal.printBits(bitsOriginais);
                System.out.println("Indice: " + i);
                Canal.printBits(bits);
            }
            c++;
        }

        return Canal.adicionaZerosAEsquerda(bitsOriginais, 8);
    }

    //recebe os dados do transmissor
    public void receberDadoBits() {
        boolean bitsVerificados[] = this.tecnica == Estrategia.CRC ? verificaDadoCRC(this.canal.recebeDado()) : verificaDadoHammig(this.canal.recebeDado());

        if (this.estaIntegro) decodificarDado(bitsVerificados);

        // Retorna verdadeiro ou falso conforme a integridade do dado definida nos métodos de verificação
        this.canal.enviaFeedBack(this.estaIntegro);
    }

    // Cria um arquivo de resultado com o conteúdo completo da mensagem decodificada com as validações
    public void gravaMensArquivo() {
        String nomeArquivo = "mensagem_recebida.txt";
        try {
            Files.write(
                    Paths.get(nomeArquivo),
                    this.mensagem.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            System.out.println("Mensagem decodificada salva em: " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao salvar a mensagem decodificada: " + e.getMessage());
        }
    }
}
