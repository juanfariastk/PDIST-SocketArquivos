package pdist_arq;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Server {
    private static final int PORT = 7001;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("== Servidor Iniciado ==");

			while (true) {
			    Socket clientSocket = serverSocket.accept();
			    System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

			    new Thread(() -> {
			        try {
			            atenderCliente(clientSocket);
			        } catch (IOException e) {
			            System.err.println("Erro ao atender cliente: " + e.getMessage());
			        }
			    }).start();
			}
		}
    }

    private static void atenderCliente(Socket clientSocket) throws IOException {
        DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

        while (true) {
            try {
                String comando = dis.readUTF();
                processarComando(comando, dos);
            } catch (IOException e) {
                System.out.println("Cliente desconectado.");
                break;
            }
        }

        dis.close();
        dos.close();
        clientSocket.close();
    }

    private static void processarComando(String comando, DataOutputStream dos) throws IOException {
        if (comando.startsWith("readdir")) {
            listarArquivos(dos);
        } else if (comando.startsWith("rename")) {
            renomearArquivo(comando, dos);
        } else if (comando.startsWith("create")) {
            criarArquivo(comando, dos);
        } else if (comando.startsWith("remove")) {
            removerArquivo(comando, dos);
        } else {
            dos.writeUTF("Comando não reconhecido.");
        }
    }

    private static void listarArquivos(DataOutputStream dos) throws IOException {
        try {
            Path dir = Paths.get(".");
            DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
            StringBuilder sb = new StringBuilder();
            for (Path entry : stream) {
                sb.append(entry.getFileName().toString()).append("\n");
            }
            dos.writeUTF(sb.length() > 0 ? sb.toString() : "Nenhum arquivo encontrado.");
        } catch (IOException e) {
            dos.writeUTF("Erro ao listar arquivos: " + e.getMessage());
        }
    }

    private static void renomearArquivo(String comando, DataOutputStream dos) throws IOException {
        String[] partes = comando.split(" ", 3);
        if (partes.length < 3) {
            dos.writeUTF("Comando inválido. Formato: rename <nome_antigo> <novo_nome>");
            return;
        }
        try {
            Files.move(Paths.get(partes[1]), Paths.get(partes[2]));
            dos.writeUTF("Arquivo renomeado com sucesso.");
        } catch (IOException e) {
            dos.writeUTF("Erro ao renomear arquivo: " + e.getMessage());
        }
    }

    private static void criarArquivo(String comando, DataOutputStream dos) throws IOException {
        String[] partes = comando.split(" ", 2);
        if (partes.length < 2) {
            dos.writeUTF("Comando inválido. Formato: create <caminho/arquivo>");
            return;
        }

        String caminhoCompleto = partes[1];
        Path caminho = Paths.get(caminhoCompleto);

        try {
            if (caminho.getParent() != null) {
                Files.createDirectories(caminho.getParent());
            }
            
            Files.createFile(caminho);
            dos.writeUTF("Arquivo criado com sucesso: " + caminhoCompleto);
        } catch (FileAlreadyExistsException e) {
            dos.writeUTF("O arquivo já existe: " + caminhoCompleto);
        } catch (IOException e) {
            dos.writeUTF("Erro ao criar arquivo: " + e.getMessage());
        }
    }


    private static void removerArquivo(String comando, DataOutputStream dos) throws IOException {
        String[] partes = comando.split(" ", 2);
        if (partes.length < 2) {
            dos.writeUTF("Comando inválido. Formato: remove <nome_arquivo>");
            return;
        }
        try {
            Files.delete(Paths.get(partes[1]));
            dos.writeUTF("Arquivo removido com sucesso.");
        } catch (IOException e) {
            dos.writeUTF("Erro ao remover arquivo: " + e.getMessage());
        }
    }
}
