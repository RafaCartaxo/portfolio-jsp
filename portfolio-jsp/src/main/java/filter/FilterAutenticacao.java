package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import servlets.ServletLogin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import connection.SingleConnectionBanco;

@WebFilter(urlPatterns = {"/principal/*"}) //Intercepta todas as requisições que vierem do projeto ou mapeamento

public class FilterAutenticacao extends HttpFilter implements Filter {
       
    private static Connection connection;
	
	
	public FilterAutenticacao() {
    }
	
    //Encerra os processos quando o servidor é parado
    //ex: kill the process of connection with database
    public void destroy() {
    	try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
    
    //Intercepta as requisições e as respostas no sistema
    //everything that pass in the system  
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
				HttpServletRequest req = (HttpServletRequest) request;
				HttpSession session = req.getSession();
				
				String usuarioLogado = (String) session.getAttribute("usuario");
				
				String urlParaAutenticar = req.getServletPath(); //URL que está sendo acessada
				
				//Validar se está logado, senão redirecionar para tela de login
				
				if(usuarioLogado == null && !urlParaAutenticar.equalsIgnoreCase("/principal/ServletLogin")) { //Não está logado
				
					RequestDispatcher redireciona = request.getRequestDispatcher("/index.jsp?url=" + urlParaAutenticar);
					request.setAttribute("msg", "Por favor, realize o login!");
					redireciona.forward(request, response);
					return; //Para a execução e redireciona para o login
					
				}else {
					chain.doFilter(request, response);
				}
				
				connection.commit(); //Tudo ok, comita no banco.
	
		}catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	//Inicia os processos ou recursos quando o servidor sobe o projeto
	//ex: inicia conexão com o banco
	public void init(FilterConfig fConfig) throws ServletException {
	connection = SingleConnectionBanco.getConnection();
	
	}

}
