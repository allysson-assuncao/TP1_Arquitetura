package org.example.tp1_arquitetura;

import java.util.Random;

//ATENÇÃO: NÃO MODIFIQUE ESTA CLASSE

public class Canal {

    private boolean[] bits;
    private Boolean feedback; //indica resultado correto do dado ou não
    private final double probRuido; //probabilidade de gerar erro em 1 único bit
    private final Random geradorAleatorio = new Random(42);

    private Transmissor transmissor; //conectado posteriormente para "simular" (poderia suprimir)
    private Receptor receptor; //conectado posteriormente para "simular"

    // Usando um polinômio convencional: 1 0 0 1 1 == x⁴ + x + 1
    public static boolean[] polinomio = {true, false, false, true, true};

    public Canal(double probRuido) {
        this.probRuido = probRuido;
    }

    public void enviarDado(boolean dados[]) {
        this.feedback = null;
        this.bits = dados;
        geradorRuido(this.bits);
        geradorAleatorio.nextInt(20);
        this.receptor.receberDadoBits();
    }

    public boolean[] recebeDado() {
        return this.bits;
    }

    public void enviaFeedBack(Boolean feedback) {
        this.bits = null;
        geradorAleatorio.nextInt(20);
        this.feedback = feedback;
    }

    public Boolean recebeFeedback() {
        return this.feedback;
    }

    public void conectaTransmissor(Transmissor trans) {
        this.transmissor = trans;
    }

    public void conectaReceptor(Receptor receptor) {
        this.receptor = receptor;
    }


    //não modifique (seu objetivo é corrigir esse erro gerado no receptor)
    private void geradorRuido(boolean bits[]) {

        //pode gerar um erro ou não..
        if (this.probRuido > 0.0 && this.geradorAleatorio.nextDouble() < this.probRuido) {
            int indice = this.geradorAleatorio.nextInt(this.bits.length);
            bits[indice] = !bits[indice];
        }

    }

    public static void printBits(boolean[] bits) {
        String vetorDeBits = "";
        vetorDeBits += "[";
        for (boolean bit : bits) {
            vetorDeBits += (bit ? "1" : "0");
        }
        vetorDeBits += "]";
        System.out.println(vetorDeBits);
    }

    // Implementação da formula de calculo de bits de paridade: 2^r - r >= K + 1
    public static int calcularBitsParidade(int k) {
        int r = 0;
        while (Math.pow(2, r) < (k + r + 1)) {
            r++;
        }
        return r;
    }

    // Esse método identifica se um número inteiro é potência de dois
    // Baseia-se no operador '&' que compara dois inteiros bit a bit com a operação AND
    public static boolean isPotenciaDeDois(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    // Retorna o índice do primeiro bit 1 (true) em um vetor de booleanos, ou -1 se não houver
    public static int ondeEstaO1(boolean[] bits) {
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                return i;
            }
        }
        return -1;
    }

    // Converte um inteiro para um vetor de boolean com sua representação em binario
    public static boolean[] intToBits(int valor) {
        if (valor == 0) return new boolean[]{false};
        int tamanho = Integer.SIZE; // Considera os zeros a esquerda

        boolean[] bits = new boolean[tamanho];
        for (int i = tamanho - 1; i >= 0; i--) {
            bits[i] = (valor & 1) == 1;
            valor >>= 1;
        }
        return bits;
    }

    // Metodo que remove os 0s à esquerda para simplificar a abstração da solução
    public static boolean[] removeZerosAEsquerda(boolean[] bits, int tamanhoMinimo) {
        if (bits.length <= tamanhoMinimo) {
            // Já está no tamanho mínimo ou menor, retorna
            return bits;
        }

        int quant0Esquerda = 0;
        // Conta zeros à esquerda
        while (quant0Esquerda < bits.length - tamanhoMinimo && !bits[quant0Esquerda]) {
            quant0Esquerda++;
        }

        // Alinha com o tamanho mínimo
        int novoTamanho = bits.length - quant0Esquerda;
        if (novoTamanho < tamanhoMinimo) {
            novoTamanho = tamanhoMinimo;
            quant0Esquerda = bits.length - tamanhoMinimo;
        }

        boolean[] resultado = new boolean[novoTamanho];
        System.arraycopy(bits, quant0Esquerda, resultado, novoTamanho - (bits.length - quant0Esquerda), bits.length - quant0Esquerda);
        return resultado;
    }

    // Adiciona 0s a esquerda para preencher um determinado tamanho de vetor, para mantes os 8 usados em caracteres UTF-8
    public static boolean[] adicionaZerosAEsquerda(boolean[] bits, int tamanho) {
        if (bits.length >= tamanho) {
            return bits;
        }
        boolean[] resultado = new boolean[tamanho];
        int offset = tamanho - bits.length;
        // Copia os elementos originais ao novo vetor a partir do offset, deixando o restante como false (0) por padrão
        System.arraycopy(bits, 0, resultado, offset, bits.length);
        return resultado;
    }

}
