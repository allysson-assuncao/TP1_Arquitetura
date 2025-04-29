package org.example.tp1_arquitetura;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        this.mensagem = carregarMensagemArquivo();
    }

    // Retorna o texto extraido do arquivo
    private String carregarMensagemArquivo() {
        try {
            return new String(Files.readAllBytes(Paths.get(arquivo.getPath())));
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
        return "";
    }

    // convertendo um símbolo para "vetor" de boolean (bits)
    private boolean[] streamCaracter(char simbolo){

        //cada símbolo da tabela ASCII é representado com 8 bits
        boolean bits[] = new boolean[8];

        //convertendo um char para int (encontramos o valor do mesmo na tabela ASCII)
        int valorSimbolo = (int) simbolo;

        //caracteres inválidos para UTF-8
        if(valorSimbolo > 255){
            valorSimbolo = 0; //quebra de linha
        }
        int indice = 7;

        //convertendo cada "bits" do valor da tabela ASCII
        while(valorSimbolo >= 2){
            int resto = valorSimbolo % 2;
            valorSimbolo /= 2;
            bits[indice] = (resto == 1);
            indice--;
        }
        bits[indice] = (valorSimbolo == 1);

        return bits;
    }

/*

    // ABSTRAÇÃO DA SOLUÇÃO

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

    // Metodo que adicionas os bits de verificação considerando a tecnica CRC
    private boolean[] dadoBitsCRC(boolean[] bitsOriginal) {

        boolean[] bits = Canal.removeZerosAEsquerda(bitsOriginal, 7);

        // Novo array com os bits originais + os 0s de verificação
        boolean[] bitsVerificacao = new boolean[bits.length + Canal.polinomio.length - 1];

        // Copia os elementos originais para o novo vetor, os espaços restantes são false (0) por padrão
        System.arraycopy(bits, 0, bitsVerificacao, 0, bits.length);

        boolean[] resto = Arrays.copyOf(bits, 5); // Copia os primeiros 5 itens de "bits"

        for (int i = 5; i < bitsVerificacao.length; ) {
            // Faz o "XOR" dos 5 elementos atuais do resto com os 5 elementos do polinômio
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
                    resto[4] = bitsVerificacao[i]; // Proximo bit
                    i++;

                    // Caso não tenham mais elementos a serem adicinados mas o resto começa em um, ainda da pra dividir
                    if (i == bitsVerificacao.length && resto[0]) {
                        // Última divisão
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
        }

        // Novo array com os bits completos
        boolean[] bitsCompletos = new boolean[bits.length + Canal.polinomio.length - 1];

        // Copia os elementos originais para um novo array
        System.arraycopy(bits, 0, bitsCompletos, 0, bits.length);

        // Copia os 4 elementos do resto ao final do novo array
        System.arraycopy(resto, 1, bitsCompletos, bits.length, Canal.polinomio.length - 1);

        return Canal.adicionaZerosAEsquerda(bitsCompletos, 8);
    }



    /*

        // ABSTRAÇÃO DA SOLUÇÃO

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

    // Metodo que adicionas os bits de verificação considerando a tecnica Hamming
    private boolean[] dadoBitsHamming(boolean[] bitsOriginal) {

        boolean[] bits = Canal.removeZerosAEsquerda(bitsOriginal, 7);

        Canal.printBits(bits);

        int quantBitsHamming = Canal.calcularBitsParidade(bits.length);

        // Novo array com os bits originais + os bits de hamming a serem definidos
        boolean[] bitsCompletos = new boolean[bits.length + quantBitsHamming];

        int c = 0;

        // Itera sobre os vetor adicionando os bits originais, "pulando" as posições de Hamming dinamicamente
        for (int i = 0; i < bitsCompletos.length; i++) {
            if (Canal.isPotenciaDeDois(i + 1)) {
                continue;
            }
            bitsCompletos[i] = bits[c];
            c++;
        }

        Canal.printBits(bitsCompletos);

        int quantidade1s = 0;

        // Itera dobre o vetor partindo so 1 (abstração)
        for (int i = 1; i <= bitsCompletos.length; i++) {

            // Caso seja um bit de hamming o indice do 1 é identificado
            if (Canal.isPotenciaDeDois(i)) {
                int indice1 = Canal.ondeEstaO1(Canal.intToBits(i));

                // Contabilizando a quantidade de 1s nos indices seguintes quem tem 1 no indice identificado
                for(int j = i + 1; j <= bitsCompletos.length; j++){
                    if (Canal.intToBits(j)[indice1] && bitsCompletos[j -1]) quantidade1s++;
                }

                // Preenche os bits de Hamming conforme a quantidade de 1s, par = 0, impar = 1
                bitsCompletos[i -1] = !(quantidade1s % 2 == 0);
                quantidade1s = 0;
            }
        }

        return Canal.adicionaZerosAEsquerda(bitsCompletos, 8);
    }

    public void enviaDado() {
        for (int i = 0; i < this.mensagem.length(); i++) {
            do {
                boolean[] bits = streamCaracter(this.mensagem.charAt(i));

                // Adicionando bits de verificação
                boolean[] bitsCompletos = this.tecnica == Estrategia.CRC ? dadoBitsCRC(bits) : dadoBitsHamming(bits);

                // Enviando cada caractere em forma de vetor de bits pela rede
                this.canal.enviarDado(bitsCompletos);
            } while (!this.canal.recebeFeedback());
            // Pedindo o reenvio da mensagem, devido a ocorrência de um erro que não pode ser coeeigido
        }
    }
}
