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



    /*

        1 0 1 0 1 0 0 -> Bit original 'T'

        // ENVIO NO TRANSMISSOR

        1 passo: como definir a quantidade de bits de verificação

        r = quantidade de bits de verificação
        K = quantidade de bits do dado original

        2^r = K + r + 1

        2⁴ = 7 + 4 + 1

        2 passo: adicionar os digitos de verificação nas posições de base 2

        001  010  011  100  101  110  111 1000 1001 1010 1011
          _    _    1    _    0    1    0    _    1    0    0

        3 passo: para definir o valor dos bits de verificação devemos fazer o XOR dos valores nas casas que tem o bit 1 na mesma posição que o do bit de verificação

        0000 0001 -> 1 ta na posicao 0
        0000 0011


        H1 = 1 + 0 + 0 + 1 + 0 = 0
        H2 = 1 + 1 + 0 + 0 + 0 = 0
        H3 = 0 + 1 + 0 = 1
        H4 = 1 + 0 + 0 = 1

        4 passo: preencher o dado com os bits adicionais e enviar

        0 0 1 1 0 1 0 1 1 0 0 -> 'T' com bits de hamming

        // VALIDAÇÃO NO RECEPTOR

        1 passo: refazer o XOR, mas dessa vez incluindo os bits de verificação

        H1 = 0 + 1 + 0 + 0 + 1 + 0 = 0
        H2 = 0 + 1 + 1 + 0 + 0 + 0 = 1 -> 2
        h3 = 1 + 0 + 1 + 0 = 0
        H4 = 1 +  1 + 0 + 0 = 1 -> 8
        h5                  = 1

        1 1 0 1 0 = 10
        2 + 8 = 10

    */

    private boolean[] dadoBitsHamming(boolean[] bitsOriginal) {        // 2^r - r >= K + 1

        boolean[] bits = removeZerosAEsquerda(bitsOriginal);

        int quantBitsHamming = Canal.calcularBitsParidade(bits.length);

        // Novo array com os bits originais + os bits de hamming a serem definidos
        boolean[] bitsCompletos = new boolean[bits.length + quantBitsHamming];

        int c = 0;
        for (int i = 0; i < bitsCompletos.length; i++) {
            if (Canal.isPotenciaDeDois(i + 1)) {
                continue;
            }
            bitsCompletos[i] = bits[c];
            c++;
        }

        int quantidade1s = 0;
        for (int i = 1; i <= bitsCompletos.length; i++) {
            if (Canal.isPotenciaDeDois(i)) {
                int indice1 = Canal.ondeEstaO1(Canal.intToBits(i));
                for(int j = i + 1; j <= bitsCompletos.length; j++){
                    if (Canal.intToBits(j)[indice1] && bitsCompletos[j -1]) quantidade1s++;
                }
                bitsCompletos[i -1] = !(quantidade1s % 2 == 0);
                quantidade1s = 0;
            }
        }

        System.out.println("Bits enviados transmissor: ");
        Canal.printBits(bitsCompletos);

        return bitsCompletos;
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
