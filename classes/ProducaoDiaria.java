package classes;

import java.sql.Date;

public class ProducaoDiaria {
    private int id;
    private Date dataProducao;
    private String produto;
    private double quantidade;
    // NOVOS CAMPOS
    private double valorBruto;
    private double lucro;

    public ProducaoDiaria() {}

    public ProducaoDiaria(int id, Date dataProducao, String produto, double quantidade, double valorBruto, double lucro) {
        this.id = id;
        this.dataProducao = dataProducao;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valorBruto = valorBruto;
        this.lucro = lucro;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDataProducao() { return dataProducao; }
    public void setDataProducao(Date dataProducao) { this.dataProducao = dataProducao; }

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public double getValorBruto() { return valorBruto; }
    public void setValorBruto(double valorBruto) { this.valorBruto = valorBruto; }

    public double getLucro() { return lucro; }
    public void setLucro(double lucro) { this.lucro = lucro; }
}
