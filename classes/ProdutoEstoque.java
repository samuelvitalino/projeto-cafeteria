package classes;

public class ProdutoEstoque {
    private int id;
    private String idCustom;
    private String nome;
    private String unidade;
    private double quantidade;
    private String tipo;

    public ProdutoEstoque() {}

    public ProdutoEstoque(int id, String idCustom, String nome, String unidade, double quantidade, String tipo) {
        this.id = id;
        this.idCustom = idCustom;
        this.nome = nome;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdCustom() { return idCustom; }
    public void setIdCustom(String idCustom) { this.idCustom = idCustom; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
