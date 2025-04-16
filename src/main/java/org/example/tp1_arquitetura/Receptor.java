package org.example.tp1_arquitetura;

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

    private void decodificarDado(boolean bits[]){
        int codigoAscii = 0;
        int expoente = bits.length-1;
        
        // Converntendo os "bits" para valor inteiro para então encontrar o valor da tabela ASCII
        for(int i = 0; i < bits.length;i++){
            if(bits[i]){
                codigoAscii += Math.pow(2, expoente);
            }
            expoente--;
        }
        
        // Concatenando cada simbolo na mensagem original
        this.mensagem += (char)codigoAscii;
    }
    
    private boolean[] verificaDadoCRC(boolean bits[]){
        
        //implemente a decodificação Hemming aqui e encontre os 
        //erros e faça as devidas correções para ter a imagem correta
        return null;
    }
    
    private boolean[] verificaDadoHammig(boolean bits[]){
        
        //implemente a decodificação Hemming aqui e encontre os 
        //erros e faça as devidas correções para ter a imagem correta
        return null;
    }
    
    
    //recebe os dados do transmissor
    public void receberDadoBits(){
        
        boolean bitsVerificados[] = this.tecnica == Estrategia.CRC ? verificaDadoCRC(this.canal.recebeDado()) : verificaDadoHammig(this.canal.recebeDado());

        decodificarDado(bitsVerificados);

        // Retorna verdadeiro ou falso conforme a integridade do dado definida nos métodos de verificação
        this.canal.enviaFeedBack(this.estaIntegro);
    }

    public void gravaMensArquivo(){
        /*
        aqui você deve implementar um mecanismo para gravar a mensagem em arquivo
        */
    }
}
