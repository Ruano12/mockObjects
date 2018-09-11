package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamento;

public class GeradorDePagamento {
	
	private final RepositorioDeLeiloes leiloes;
	private final Avaliador avaliador;
	private final RepositorioDePagamento pagamentos;

	public GeradorDePagamento(RepositorioDeLeiloes leiloes, RepositorioDePagamento pagamentos, Avaliador avaliador) {
		this.leiloes = leiloes;
		this.pagamentos = pagamentos;
		this.avaliador = avaliador;
	}

	public void gera() {
		List<Leilao> leiloesEncerrados = this.leiloes.encerrados();
		
		for(Leilao leilao : leiloesEncerrados) {
			this.avaliador.avalia(leilao);
			
			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), Calendar.getInstance());
			this.pagamentos.salva(novoPagamento);
		}
	}
}
