package classes;

import java.sql.Date;

public class EntradaEstoque {
    private int id;
    private int produtoId;
    private double quantidade;
    private String notaFiscal;
    private Date dataEntrada;

    public EntradaEstoque() {}

    public EntradaEstoque(int id, int produtoId, double quantidade, String notaFiscal, Date dataEntrada) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.notaFiscal = notaFiscal;
        this.dataEntrada = dataEntrada;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getNotaFiscal() { return notaFiscal; }
    public void setNotaFiscal(String notaFiscal) { this.notaFiscal = notaFiscal; }

    public java.sql.Date getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(java.sql.Date dataEntrada) { this.dataEntrada = dataEntrada; }
}
