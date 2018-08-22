import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import org.junit.Test;
import org.mockito.InOrder;
import org.omg.CORBA.RepositoryIdHelper;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao;
import br.com.caelum.leilao.infra.dao.LeilaoDaoFalso;
import br.com.caelum.leilao.servico.EncerradorDeLeilao;
import br.com.caelum.leilao.servico.EnviadorDeEmail;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);		
		
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		

		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		assertEquals(2, encerrador.getTotalEncerrados());
		assertTrue(leilao1.isEncerrado());
		assertTrue(leilao2.isEncerrado());		
	}
	
	 @Test
	 public void naoDeveEncerrarLeiloesCasoNaoHajaNenhum() {

        RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
        when(daoFalso.correntes()).thenReturn(new ArrayList<Leilao>());

        EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
        EncerradorDeLeilao encerrador = 
            new EncerradorDeLeilao(daoFalso, carteiroFalso);

        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
	 }
	 
	 @Test
	 public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
		 Calendar ontem = Calendar.getInstance();
	     ontem.set(ontem.get(Calendar.YEAR), ontem.getTime().getMonth(), ontem.getTime().getDate()-1);
	     System.out.println(ontem.getTime());
	     Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
	     Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();
	     
	     RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
	     when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
	     
	     EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
	     EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
	     
	     encerrador.encerra();
	     
	     
	     assertEquals(0, encerrador.getTotalEncerrados());
	     assertFalse(leilao1.isEncerrado());
	     assertFalse(leilao2.isEncerrado());

	     verify(daoFalso, never()).atualiza(leilao1);
	     verify(daoFalso, never()).atualiza(leilao2);
	 }
	 
	 @Test
	 public void deveEnviarEmailAposPersistirLeilaoEncerrado() {
		 Calendar antiga = Calendar.getInstance();
	     antiga.set(1999, 1, 20);
	
	     Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
	 
	     RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
	     when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

	     EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
	     EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
	     
	     encerrador.encerra();
	     
	     InOrder inOrder = inOrder(daoFalso, carteiroFalso);
	     inOrder.verify(daoFalso, times(1)).atualiza(leilao1);
	     inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
	 }
	
	@Test
	public void deveAtualizarLeiloesEncerrados() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1990, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("Tv de Plasma").naData(antiga).constroi();
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		EnviadorDeEmail carteiroFalso = mock(EnviadorDeEmail.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao1);
		
	}
}
