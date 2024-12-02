package pdist_arq;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) throws IOException {
    	 System.out.println("== Cliente Iniciado ==");

         try (Socket socket = new Socket("127.0.0.1", 7001)) {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			 DataInputStream dis = new DataInputStream(socket.getInputStream());

			 try (Scanner teclado = new Scanner(System.in)) {
				while (true) {
				     System.out.print("Digite um comando (readdir, rename, create, remove): ");
				     String comando = teclado.nextLine();

				     dos.writeUTF(comando);

				     String resposta = dis.readUTF();
				     System.out.println("Resposta do servidor: " + resposta);
				 }
			}
		}
    }
}
