package classes;

import java.sql.Timestamp;

public class Pedido {
    private int id;
    private String idCustom;
    private String clienteNome;
    private String status;
    private Timestamp dataPedido;

    public Pedido() {}

    public Pedido(int id, String idCustom, String clienteNome, String status, Timestamp dataPedido) {
        this.id = id;
        this.idCustom = idCustom;
        this.clienteNome = clienteNome;
        this.status = status;
        this.dataPedido = dataPedido;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdCustom() { return idCustom; }
    public void setIdCustom(String idCustom) { this.idCustom = idCustom; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getDataPedido() { return dataPedido; }
    public void setDataPedido(Timestamp dataPedido) { this.dataPedido = dataPedido; }
}

