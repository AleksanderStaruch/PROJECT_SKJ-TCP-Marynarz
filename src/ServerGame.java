import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class ServerGame implements Runnable {
    private static final String NAME="SERVER GAME";
    private static final int PORT=4000;
    private static List<Player> players=new ArrayList<>();
    private static List<String> wyniki=new ArrayList<>();
    private static Map<String,Integer> namesAndPorts=new LinkedHashMap<>();
    private static Map<String,InetAddress> namesAndIp=new LinkedHashMap<>();
    private Socket playerSocket;

    private ServerGame(Socket playerSocket) {
        this.playerSocket = playerSocket;
    }

    private static void log(String message){
        System.out.println(NAME+": "+message);
        System.out.flush();
    }

    private String getClientInfo(Socket clientSocket){
        String clientIP=clientSocket.getInetAddress().getHostAddress();
        int clientPort = clientSocket.getPort();

        return "[" + clientIP + "]:"+clientPort;
    }

    private static List<String> scanner(){
        List<String> html=new ArrayList<>();
        try{ html=Files.lines(Paths.get("html.txt")).collect(Collectors.toList()); }
        catch(Exception ex){ ex.printStackTrace(); }
        return html;
    }

    private static void newHtml(List<String> html){
        try(FileWriter fw = new FileWriter(new File("wielki turniej.html"))){
            Files.write(Paths.get("wielki turniej.html"), html, StandardOpenOption.APPEND);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void html(){
        try(FileWriter fw = new FileWriter(new File("wielki turniej.html"))){
            List<String> temp =new ArrayList<>();
            for(String h: scanner()){
                temp.add(h+"\r\n");

                if(h.equals("<p><b>Gracze w grze: </b></p>")){
                    for(int i=0;i<players.size();i++){
                        temp.add("<p>"+(i+1)+". "+players.get(i).getName()+"</p>"+"\r\n");
                    }
                }

                if(h.equals("<p><b>Wyniki: </b></p>")){
                    for(String s:wyniki){
                        temp.add("<p>"+s+"</p>"+"\r\n");
                    }
                }
            }
            Files.write(Paths.get("wielki turniej.html"), temp, StandardOpenOption.APPEND);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    private static List<String> wynikiGracza(String gracz){
        List<String> tmp = new ArrayList<>();
        for(String s:wyniki){
            String imie1=s.split(" ")[0];
            String imie2=s.split(" ")[2];
            if(gracz.equals(imie1) || gracz.equals(imie2)){
                tmp.add(s);
            }
        }
        return tmp;
    }

    private static int n=5001;
    public void run() {
        String name=null;
        try {
            log("Client connected: "+getClientInfo(playerSocket));
            log("Stream collecting");

            InputStream sis = playerSocket.getInputStream();
            OutputStream sos = playerSocket.getOutputStream();
            InputStreamReader sisr = new InputStreamReader(sis);
            OutputStreamWriter sosr = new OutputStreamWriter(sos);
            BufferedReader br = new BufferedReader(sisr);
            BufferedWriter bw = new BufferedWriter(sosr);
            Player player;

            name =br.readLine();
            log("PLAYER: "+name);
            bw.write("HELLO "+name);
            bw.newLine();
            bw.flush();

            namesAndPorts.put(name,n);
            namesAndIp.put(name,playerSocket.getInetAddress());

            bw.write((n++)+"");
            bw.newLine();
            bw.flush();
            boolean czyGra =true;
            while(czyGra){
                String command = br.readLine();
                if(command!=null){
                    log("PLAYER "+name+ " SAID : "+ command);
                    String[] tab= command.split(" ");//przez kogo zaproszony
                    boolean friend=true;
                    if(tab.length==2 && tab[0].equals("JOIN")){
                        if(!Player.getNames().contains(tab[1])){
                                if(Player.getNames().contains(name)){
                                    player=new Player(
                                            tab[1],namesAndPorts.get(tab[1]),
                                            namesAndPorts.get(name),namesAndIp.get(name)
                                    );
                                    players.add(player);

                                    log("PLAYER "+tab[1]+ " IS ADDED TO LIST");
                                    bw.write("YOU ARE ADDED TO LIST OF THE PLAYERS");
                                    bw.newLine();
                                    bw.flush();

                                    friend=false;
                                    html();
                                }

                        }else{
                            log("PLAYER IS ALREADY ON THE LIST");
                            bw.write("YOU ARE ALREADY ON THE LIST");
                            bw.newLine();
                            bw.flush();
                            friend=false;
                        }
                        if(friend){
                            log("PLAYER IS NOT ADDED TO LIST");
                            bw.write("IF YOU WANT TO JOIN ASK PLAYER ON SERVER lIST");
                            bw.newLine();
                            bw.flush();
                        }
                    }else{
                        switch(command){
                            case "JOIN":
                                if(players.size()==0){
                                    log("PLAYER "+name+" IS ADDED TO LIST");
                                    bw.write("YOU ARE ADDED TO LIST OF THE PLAYERS");
                                    bw.newLine();
                                    bw.flush();
                                    player=new Player(
                                            name,namesAndPorts.get(name),
                                            namesAndPorts.get(name),playerSocket.getInetAddress()
                                    );
                                    players.add(player);
                                    html();//html
                                }else{
                                    if(Player.getNames().contains(name)){
                                        log("PLAYER IS ALREADY ON THE LIST");
                                        bw.write("YOU ARE ALREADY ON THE LIST");
                                        bw.newLine();
                                        bw.flush();
                                    }else{
                                        log("PLAYER IS NOT ADDED TO LIST");
                                        bw.write("IF YOU WANT TO JOIN ASK PLAYER ON SERVER lIST");
                                        bw.newLine();
                                        bw.flush();
                                    }
                                }
                                break;
                            case "LIST":
                                if(Player.getNames().contains(name)){
                                    log("SERVER SENDS LIST OF THE PLAYERS");
                                    bw.write("LIST OF PLAYERS");
                                    bw.newLine();
                                    bw.flush();
                                    String lista="";
                                    for(Player p: players){
                                        lista+=(p+"-");
                                    }
                                    bw.write(lista);
                                    bw.newLine();
                                    bw.flush();
                                }else{
                                    log("SERVER DOESN'T SEND LIST OF THE PLAYERS");
                                    bw.write("YOU ARE NOT ON THE LIST, YOU CAN NOT SEE IT");
                                    bw.newLine();
                                    bw.flush();
                                }
                                break;
                            case "WYNIK":
                                command=br.readLine();
                                wyniki.add(command);
                                html();
                                break;
                            case "CLIENT PORT":
                                command=br.readLine();
                                if(Player.getNames().contains(command)){
                                    int index=Player.getNames().indexOf(command);
                                    int port=Player.getPorts().get(index);
                                    bw.write(port+"");
                                    bw.newLine();
                                    bw.flush();
                                }else{
                                    bw.write("PLAYER WITH THIS NAME DOESN'T EXIST");
                                    bw.newLine();
                                    bw.flush();
                                }
                                break;
                            case "QUIT":
                                List<String> temp = wynikiGracza(name);
                                for(String p: temp){
                                    wyniki.remove(p);
                                }
                                for(int i=0;i<players.size();i++){
                                    if(players.get(i).getName().equals(name)){
                                        players.remove(i);
                                        break;
                                    }
                                }
                                html();
                                czyGra=false;
                                break;
                            default:
                                log("ERROR BAD COMMAND!");
                                bw.write("ERROR BAD COMMAND!");
                                bw.newLine();
                                bw.flush();
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log("Error handling PLAYER " + name + ": " + e);
        } finally {
            try {
                log("PLAYER playerSocket closing");
                playerSocket.close();
                log("Connection with PLAYER " + name + " closed");
                log("PLAYER playerSocket closed");
            } catch (IOException e) {
                log("Couldn't close a playerSocket, what's going on?");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        boolean working=true;
        log("Start");
        log("ServerGame serverSocket creation");
        ServerSocket serverSocket= new ServerSocket(PORT);
        log("ServerGame serverSocket created");

        log("Website starting");
        newHtml(scanner());
        File htmlFile = new File("wielki turniej.html");
        Desktop.getDesktop().browse(htmlFile.toURI());

        log("ServerGame listening");
        try {
            while (working) {
                Thread thread=new Thread(new ServerGame(serverSocket.accept()));
                thread.start();
                sleep(5000);
            }
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }finally {
            log("Service ended");
            log("ServerGame serverSocket closing");
            serverSocket.close();
            log("ServerGame serverSocket closed");
            log("End");
        }
    }
}