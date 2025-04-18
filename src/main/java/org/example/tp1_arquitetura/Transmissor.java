package org.example.tp1_arquitetura;

import java.io.File;
import java.util.Arrays;

public class Transmissor {
    private String mensagem;
    private final Canal canal;
    private File arquivo;
    private final Estrategia tecnica;

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
        boolean[] bits = new boolean[8];

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

    T → 84  → 1 0 1 0 1 0 0
    e → 101 → 1 1 0 0 1 0 1
    s → 115 → 1 1 1 0 0 1 1
    t → 116 → 1 1 1 0 1 0 0
    e → 101 → 1 1 0 0 1 0 1

    POLI: 1 0 0 1 1 → Grau 4

    1) Acrescentamos ao final do dado uma quantidade de zeros igual ao grau do polinômio (tamanho em bits menos 1).

        1 0 1 0 1 0 0 [ 0 0 0 0 ]

    2) Realizamos uma divisão binária do dado "completo" (dado + zeros) pelo polinômio, usando a operação XOR.

        1 0 1 0 1 0 0 0 0 0 0 | 1 0 0 1 1
        1 0 0 1 1 | | | | | |
        --------- ↓ ↓ | | | |
        0 0 1 1 0 0 0 | | | |
            1 0 0 1 1 | | | |
            --------- ↓ | | |
            0 1 0 1 1 0 | | |
              1 0 0 1 1 | | |
              --------- ↓ ↓ |
              0 0 1 0 1 0 0 |
                  1 0 0 1 1 |
                  --------- ↓
                  0 0 1 1 1 0 → Resto

    3. O resto dessa divisão é o CRC (4 bits).

        1 1 1 0

    4. Anexamos o CRC ao final do dado original (sem os zeros) e enviamos esse pacote completo.

        1 0 1 0 1 0 0 [ 1 1 1 0 ]

    5. No receptor, o mesmo processo de divisão é feito, usando o mesmo polinômio.

        1 0 1 0 1 0 0 1 1 1 0 | 1 0 0 1 1
        1 0 0 1 1 | | | | | |
        --------- ↓ ↓ | | | |
        0 0 1 1 0 0 0 | | | |
            1 0 0 1 1 | | | |
            --------- ↓ | | |
            0 1 0 1 1 1 | | |
              1 0 0 1 1 | | |
              --------- ↓ ↓ |
              0 0 1 0 0 1 1 |
                  1 0 0 1 1 |
                  --------- ↓
                  0 0 0 0 0 0 → Resto

    6. Se o resto da divisão no receptor for zero, os dados foram recebidos corretamente. Se o resto for diferente de zero, ocorreu erro na transmissão.

    O valor retornado é o original: 1 0 1 0 1 0 0, sem os bits de verificação



        1 1 0 0 0 1 0 0 0 0 0 | 1 0 0 1 1
        1 0 0 1 1 | | | | | |
        --------- ↓ | | | | |
        0 1 0 1 1 1 | | | | |
          1 0 0 1 1 | | | | |
          --------- ↓ ↓ | | |
          0 0 1 0 0 0 0 | | |
              1 0 0 1 1 | | |
              --------- ↓ ↓ ↓
              0 0 0 1 1 0 0 0
                    1 0 0 1 1
                    ---------
                    0 1 0 1 1

*/

    public static boolean[] removeZerosAEsquerda(boolean[] bits) {
        int firstTrue = 0;
        while (firstTrue < bits.length && !bits[firstTrue]) {
            firstTrue++;
        }
        if (firstTrue == 0) {
            // Não há falses à esquerda
            return bits;
        }
        if (firstTrue == bits.length) {
            // Todos são falses, retorna um vetor vazio
            return new boolean[0];
        }
        boolean[] result = new boolean[bits.length - firstTrue];
        System.arraycopy(bits, firstTrue, result, 0, result.length);
        return result;
    }

    private boolean[] dadoBitsCRC(boolean[] bitsOriginal) {

        boolean[] bits = removeZerosAEsquerda(bitsOriginal);

        // Novo array com os bits originais + os 0s de verificação
        boolean[] bitsVerificacao = new boolean[bits.length + Canal.polinomio.length - 1];

        // Copia os elementos originais para o novo vetor, os espaços restantes são false (0) por padrão
        System.arraycopy(bits, 0, bitsVerificacao, 0, bits.length);

        boolean[] resto = Arrays.copyOf(bits, 5); // Copia os primeiros 5 itens de "bits"

        for (int i = 5; i < bitsVerificacao.length; ) {
            // Faz o XOR dos 5 elementos atuais do resto com os 5 elementos do polinômio
            for (int j = 0; j < 5; j++) {
                resto[j] = resto[j] != Canal.polinomio[j];
            }
            /*System.out.println("Resto: ");
            Canal.printBits(resto);*/

            // Retira os 0s a esquerda do resto e pega os próximos números de bitsVerificacao, quando há
            for (int j = 0; j < 5; j++) {
                if (!resto[j]) {
                    resto[0] = resto[1];
                    resto[1] = resto[2];
                    resto[2] = resto[3];
                    resto[3] = resto[4];
                    resto[4] = bitsVerificacao[i];
                    i++;
                    if (i == bitsVerificacao.length && resto[0]) {
                        for (int c = 0; c < 5; c++) {
                            resto[c] = resto[c] != Canal.polinomio[c];
                        }
                        break;
                    }
                    j = -1;
                    if (i >= bitsVerificacao.length) {
                        break;
                    }
                } else break;
            }
            /*System.out.println("Resto sem zeros a esquerda: ");
            Canal.printBits(resto);*/
        }

        // Novo array com os bits completos
        boolean[] bitsCompletos = new boolean[bits.length + Canal.polinomio.length - 1];

        // Copia os elementos originais para um novo array
        System.arraycopy(bits, 0, bitsCompletos, 0, bits.length);

        // Copia os 4 elementos do resto ao final do novo array
        System.arraycopy(resto, 1, bitsCompletos, bits.length, Canal.polinomio.length - 1);

        /*System.out.println("Bits completos: ");
        Canal.printBits(bitsCompletos);*/

        return bitsCompletos;
    }

    private boolean[] dadoBitsHamming(boolean[] bits) {

        return bits;
    }

    public void enviaDado() {
        for (int i = 0; i < this.mensagem.length(); i++) {
            do {
                boolean[] bits = streamCaracter(this.mensagem.charAt(i));

                /*System.out.println("Letra atual: ");
                Canal.printBits(bits);*/

                // Adicionando bits de verificação
                boolean[] bitsCompletos = this.tecnica == Estrategia.CRC ? dadoBitsCRC(bits) : dadoBitsHamming(bits);

                // Enviando cada caractere em forma de vetor de bits pela rede
                this.canal.enviarDado(bitsCompletos);
            } while (!this.canal.recebeFeedback());
            // Pedindo o reenvio da mensagem, devido a ocorrência de um erro que não pode ser coeeigido
        }
    }
}
