import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable{
    private static boolean czyNaLiscie=false;
    private static boolean czyZajety=false;
    private static boolean czyRozegraneGry=false;
    private static List<String> names=new ArrayList<>();
    private static List<Integer> ports=new ArrayList<>();
    private static List<Boolean> rozegranyMecz=new ArrayList<>();
    private static List<String> wyniki=new ArrayList<>();
    private static Socket clientSocket;
    private static BufferedReader br;
    private static BufferedWriter bw;
    private static String response;
    private static String myName ="",number="";
    private static boolean working=true;

    private static List<String> wynikiGracza(String gracz){
        List<String> tmp = new ArrayList<>();
        for(String s:wyniki){
            String imie2=s.split(" ")[2];
            String imie1=s.split(" ")[0];
            if(gracz.equals(imie1) || gracz.equals(imie2)){
                tmp.add(s);
            }
        }
        return tmp;
    }

    private int n;
    private Client(int n) {
        this.n=n;
    }
    public void run(){
        try {
            ServerSocket serverSocket= new ServerSocket(n);
            while(working){
                new Server(serverSocket.accept()).start();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static boolean mecz(int liczba1 ,int liczba2,boolean zaczynamJa){
        int sum=liczba1+liczba2;
        boolean wynik;
        if(zaczynamJa){
            //zaczynamy od gracza 1
//            if(sum%2==0){
//                //s=gracz1+" > "+gracz2;
//                wynik=false;
//            }else{
//                //s=gracz1+" < "+gracz2;
//                wynik=true;
//            }
            wynik=!(sum%2==0);
        }else{
            //zaczynamy od gracza 2
//            if(sum%2==0){
//                //s=gracz1+" < "+gracz2;
//                wynik=true;
//            }else{
//                //s=gracz1+" > "+gracz2;
//                wynik=false;
//            }
            wynik=(sum%2==0);
        }
        return wynik;
    }

    private static class Play extends Thread {
        private Play() {}
        public void run() {
            int counter=0;
            String response;
            try {
                sleep(5000);
                while (working) {
                    sleep(1000);
                    if(czyNaLiscie){
                        clientSocket=socket(sockets,4000);
                        bw.write("LIST");
                        bw.newLine();
                        bw.flush();
                        response=br.readLine()+"";
                        if (response.equals("LIST OF PLAYERS")) {
                            response = br.readLine();
                            String[] tab = response.split("-");
                            int i=counter;
                            //czy sa nowi gracze
                            if(counter<tab.length){
                                counter=tab.length;
                            }
                            //pobieranie nowych garczy
                            for(;i<counter;i++){
                                names.add(tab[i].split(" ")[0]);
                                ports.add(Integer.parseInt(tab[i].split(" ")[1]));
                                rozegranyMecz.add(false);
                            }
                            //usuwanie siebie z listy
                            if(names.contains(myName)){
                                int n=names.indexOf(myName);
                                names.remove(n);
                                ports.remove(n);
                                rozegranyMecz.remove(n);
                            }
                        }
                        if(!czyZajety){
                            czyZajety=true;
                            Random random=new Random();
                            boolean czyJa= random.nextBoolean();
                            int index;
                            if(rozegranyMecz.contains(false)){
                                czyRozegraneGry=false;
                                index=rozegranyMecz.indexOf(false);
                                rozegranyMecz.set(index,true);

                                clientSocket=socket(sockets,ports.get(index));
                                String przeciwnik=names.get(index);
                                bw.write("PLAY");
                                bw.newLine();
                                bw.flush();
                                response=br.readLine();
                                if(response.equals("YES")){
                                    bw.write(czyJa+"");
                                    bw.newLine();
                                    bw.flush();
                                    response=br.readLine();
                                    int przeciwnikN=Integer.parseInt(response);
                                    bw.write(number);
                                    bw.newLine();
                                    bw.flush();
                                    boolean wynik=mecz(Integer.parseInt(number),przeciwnikN,czyJa);
                                    String kto=myName;
                                    if(!czyJa){
                                        kto=przeciwnik;
                                    }

                                    if(wynik){
                                        clientSocket=socket(sockets,4000);
                                        bw.write("WYNIK");
                                        bw.newLine();
                                        bw.flush();
                                        wyniki.add(myName +" < "+przeciwnik+" odliczanie zaczelo sie od "+kto);
                                        bw.write(myName +" < "+przeciwnik+" odliczanie zaczelo sie od "+kto);
                                        bw.newLine();
                                        bw.flush();
                                    }else{
                                        wyniki.add(myName +" > "+przeciwnik+" odliczanie zaczelo sie od "+kto);
                                    }
                                }
                                czyZajety=false;
                            }else{
                                czyRozegraneGry=true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    private class Server extends Thread  {
        private Socket socket;
        private Server(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                InputStream sisR = socket.getInputStream();
                OutputStream sosR = socket.getOutputStream();
                InputStreamReader sisrR = new InputStreamReader(sisR);
                OutputStreamWriter sosrR = new OutputStreamWriter(sosR);
                BufferedReader brR = new BufferedReader(sisrR);
                BufferedWriter bwR = new BufferedWriter(sosrR);
                String response,name;

                name =brR.readLine();
                bwR.write("HELLO "+name);
                bwR.newLine();
                bwR.flush();

                while (working) {
                    response=brR.readLine();
                    switch(response){
                        case "JOIN":
                            clientSocket= socket(sockets,4000);
                            bw.write("JOIN "+name);
                            bw.newLine();
                            bw.flush();
                            response=br.readLine();

                            bwR.write(response);
                            bwR.newLine();
                            bwR.flush();
                            break;
                        case "PLAY":
                            if(!czyZajety){
                                bwR.write("YES");
                                bwR.newLine();
                                bwR.flush();
                                czyZajety=true;
                                response=brR.readLine();
                                boolean czyPrze=response.equals("true");
                                bwR.write(number);
                                bwR.newLine();
                                bwR.flush();
                                response=brR.readLine();

                                int przeciwnikN=Integer.parseInt(response);

                                boolean wynik=mecz(Integer.parseInt(number),przeciwnikN,czyPrze);//mecz
                                String kto=myName;
                                if(czyPrze){
                                    kto=name;
                                }
                                int index = names.indexOf(name);
                                if(index>=0){
                                    rozegranyMecz.set(index,true);
                                }

                                if(!wynik){
                                    clientSocket=socket(sockets,4000);
                                    bw.write("WYNIK");
                                    bw.newLine();
                                    bw.flush();
                                    wyniki.add(name+" > "+myName+" odlicznie zaczelo sie od "+kto);
                                    bw.write(name+" > "+myName+" odlicznie zaczelo sie od "+kto);
                                    bw.newLine();
                                    bw.flush();
                                }else{
                                    wyniki.add(name+" < "+myName+" odlicznie zaczelo sie od "+kto);
                                }
                            }else{
                                bwR.write("NO");
                                bwR.newLine();
                                bwR.flush();
                            }
                            czyZajety=false;
                            break;
                        case "QUIT":
                            List<String> tmp=wynikiGracza(name);
                            for(String s:tmp){
                                wyniki.remove(s);
                            }
                            System.out.println("Player "+name+" close connection");
                            socket.close();
                            break;
                        default:
                            bwR.write("ERROR BAD COMMAND");
                            bwR.newLine();
                            bwR.flush();
                            break;
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Socket> sockets=new ArrayList<>();
    private static Socket socket(List<Socket> sockets,int n)throws IOException{
        final String serverName="localhost";
        InetAddress serverIP = InetAddress.getByName(serverName);

        InputStream sis;
        OutputStream sos;
        InputStreamReader sisr;
        OutputStreamWriter sosr;
        Socket socket;
        for(Socket s:sockets){
            if(s.getPort()==n){
                clientSocket=s;
                sis= clientSocket.getInputStream();
                sos= clientSocket.getOutputStream();
                sisr= new InputStreamReader(sis);
                sosr= new OutputStreamWriter(sos);
                br= new BufferedReader(sisr);
                bw= new BufferedWriter(sosr);

                return s;
            }
        }
        socket= new Socket(serverIP,n);
        clientSocket=socket;
        sis= clientSocket.getInputStream();
        sos= clientSocket.getOutputStream();
        sisr= new InputStreamReader(sis);
        sosr= new OutputStreamWriter(sos);
        br= new BufferedReader(sisr);
        bw= new BufferedWriter(sosr);

        //wyslanie imienia do servera i czekanie na
        bw.write(myName);
        bw.newLine();
        bw.flush();
        response=br.readLine();
        sockets.add(socket);

        return socket;

    }

    public static void main(String[] args)throws Exception{
        //imie i liczba
        if(args.length > 0) { number = args[0];myName =args[1]; }
        System.out.println("Player: "+ myName +" chose number: "+number);///pozniej port comenda port comenda

        //dokogo i komendy
        List<String> commands=new ArrayList<>();
        List<String> toWhom=new ArrayList<>();
        for(int i=2;i<args.length;i+=2){
            commands.add(args[i+1]);
            toWhom.add(args[i]);
        }

        //polaczenie z serwerem
        clientSocket= socket(sockets,4000);

        //klient gotowy do rozmowy z innymi klientami
        int port = Integer.parseInt(br.readLine());//port klienta do rozmowy z innymi agentami
        Thread thread=new Thread(new Client(port));thread.start();//rozmowa z innymi agentami

        //PLAY mecze
        Thread letsplay=new Thread(new Play());letsplay.start();

        int counter=0;
        String comm,sc;
        while (working){
            comm="";response="";
            //wczytanie reszty args a pozniej mozna dzialac na klawiaturze
            if(counter==commands.size()){
                sc=new Scanner(System.in).nextLine();
                if(sc.split(" ").length==2){
                    String dokogo=sc.split(" ")[0];
                    if(dokogo.equals("server")){
                        clientSocket=socket(sockets,4000);
                        comm=sc.split(" ")[1];
                    }else{
                        clientSocket=socket(sockets,4000);
                        bw.write("CLIENT PORT");
                        bw.newLine();
                        bw.flush();
                        bw.write(dokogo);
                        bw.newLine();
                        bw.flush();
                        response=br.readLine();

                        clientSocket=socket(sockets,Integer.parseInt(response));
                        comm=sc.split(" ")[1];//test
                    }
                }else{
                    if(sc.equals("QUIT")){
                        if(czyRozegraneGry) {//quit  zadzala gdy rozegramy wszystkie gry
                            clientSocket = socket(sockets, 4000);
                            bw.write("QUIT");
                            bw.newLine();
                            bw.flush();

                            for(String s:wyniki){
                                System.out.println(s);
                            }
                            for(int i:ports){
                                clientSocket=socket(sockets,i);
                                bw.write("QUIT");
                                bw.newLine();
                                bw.flush();
                                clientSocket.close();
                            }

                            working = false;
                        }
                    }
                }
            }else{
                if(toWhom.get(counter).equals("server")){
                    clientSocket=socket(sockets,4000);
                    comm=commands.get(counter++);//test
                    System.out.println("server "+comm);
                }else{
                    String dokogo=toWhom.get(counter);

                    clientSocket=socket(sockets,4000);
                    bw.write("CLIENT PORT");
                    bw.newLine();
                    bw.flush();
                    bw.write(dokogo);
                    bw.newLine();
                    bw.flush();
                    response=br.readLine();
                    if (!response.equals("PLAYER WITH THIS NAME DOESN'T EXIST")){
                        clientSocket=socket(sockets,Integer.parseInt(response));
                        comm=commands.get(counter++);//test
                        System.out.println(dokogo+" "+comm);
                    }else{
                        comm=commands.get(counter++);//test
                        System.out.println(dokogo+" "+comm);
                        System.out.println(response);
                    }
                }
            }
            //wczytanie reszty args a pozniej mozna dzialac na klawiaturze (KONIEC)

            //wysylanie commendy i odbior
            if(working && !response.equals("PLAYER WITH THIS NAME DOESN'T EXIST")){
                bw.write(comm);
                bw.newLine();
                bw.flush();
                response = br.readLine();System.out.println(response);
                if (response.equals("YOU ARE ADDED TO LIST OF THE PLAYERS")) {
                    czyNaLiscie=true;
                }
                if (response.equals("LIST OF PLAYERS")) {
                    response = br.readLine();
                    String[] tab = response.split("-");
                    for(int i=0;i<tab.length;i++){
                        System.out.println((i+1)+". "+tab[i]);
                    }
                }
                response="";
            }
            //wysylanie commendy i odbior (KONIEC)
        }
        //QUIT
        try {
            clientSocket.close();
            System.out.println("STOP");
        }catch (Exception ex){
            //ex.printStackTrace();
        }

    }
}