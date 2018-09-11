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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Matchers.any;
import org.junit.Test;
import org.mockito.InOrder;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.email.Carteiro;
import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.servico.EncerradorDeLeilao;

public class EncerradorDeLeilaoTest {

	@Test
	public void deveEncerrarLeiloesQueComecaramUmaSemanaAntes() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);		
		
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		Carteiro carteiroFalso = mock(Carteiro.class);
		
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

        Carteiro carteiroFalso = mock(Carteiro.class);
        EncerradorDeLeilao encerrador = 
            new EncerradorDeLeilao(daoFalso, carteiroFalso);

        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
	 }
	 
	 @Test
	 public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
		 Calendar ontem = Calendar.getInstance();
	     Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(ontem).constroi();
	     Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(ontem).constroi();
	     
	     RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
	     when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
	     
	     Carteiro carteiroFalso = mock(Carteiro.class);
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

	     Carteiro carteiroFalso = mock(Carteiro.class);
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
		Carteiro carteiroFalso = mock(Carteiro.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao1);
		
	}
	
	@Test
	public void deveContinuarAExecucaoMesmoQuandoDaoFalha() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999,  1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
	    Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
	     
	    RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		Carteiro carteiroFalso = mock(Carteiro.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		verify(daoFalso, times(1)).atualiza(leilao2);
		verify(carteiroFalso).envia(leilao2);
		
		verify(carteiroFalso, times(0)).envia(leilao1);
	}
	
	@Test
	public void deveContinuaTodosMasNãoEnviarEmail() {
		Calendar antiga = Calendar.getInstance();
		antiga.set(1999,  1, 20);
		
		Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma").naData(antiga).constroi();
	    Leilao leilao2 = new CriadorDeLeilao().para("Geladeira").naData(antiga).constroi();
	    Leilao leilao3 = new CriadorDeLeilao().para("Xbox one x").naData(antiga).constroi();
	    Leilao leilao4 = new CriadorDeLeilao().para("Rack").naData(antiga).constroi();
	     
	    RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		Carteiro carteiroFalso = mock(Carteiro.class);
		
		when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2, leilao3, leilao4));
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao2);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao3);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao4);
		
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		encerrador.encerra();
		
		
		verify(carteiroFalso, times(0)).envia(any(Leilao.class));
	}
}
