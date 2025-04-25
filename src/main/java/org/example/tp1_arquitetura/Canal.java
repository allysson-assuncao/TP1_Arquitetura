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

    // Usando o polinômio convencional 1 0 0 1 1 == x⁴ + x + 1
    public static boolean[] polinomio = {true, false, false, true, true};

    public Canal(double probRuido) {this.probRuido = probRuido;}

    public void enviarDado(boolean dados[]) {
        this.feedback = null;
        this.bits = dados;
        geradorRuido(this.bits);
        try {
            Thread.sleep(geradorAleatorio.nextInt(20) + 37);
        } catch (InterruptedException ex) {
            System.err.println("processo interrompido durante o envio do dado");
        }
        this.receptor.receberDadoBits();
    }

    public boolean[] recebeDado() {
        return this.bits;
    }

    public void enviaFeedBack(Boolean feedback) {
        this.bits = null;
        try {
            Thread.sleep(geradorAleatorio.nextInt(20));
        } catch (InterruptedException ex) {
            System.err.println("processo interrompido durante o envio do dado");
        }
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
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < bits.length; i++) {
            sb.append(bits[i] ? "1" : "0");
            if (i < bits.length - 1) {
                sb.append(" ");
            }
        }
        sb.append("]");
        System.out.println(sb);
    }

    public static int calcularBitsParidade(int k) {
        int r = 0;
        while (Math.pow(2, r) < (k + r + 1)) {
            r++;
        }
        return r;
    }

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
        int tamanho = Integer.SIZE;

        boolean[] bits = new boolean[tamanho];
        for (int i = tamanho - 1; i >= 0; i--) {
            bits[i] = (valor & 1) == 1;
            valor >>= 1;
        }
        return bits;
    }

}
