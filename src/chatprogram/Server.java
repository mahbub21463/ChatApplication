/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatprogram;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mahbub
 */
public class Server extends Thread {

    private int port;
    private boolean serverOn;
    private ServerSocket ss;
    private ArrayList<ClientThread> clients;

    public Server(int p) throws IOException {
        port = p;
        clients = new ArrayList<ClientThread>();
        ss = new ServerSocket(5555);
    }

    //override
    public void run() {

        while (true) {
            try {
                System.out.println("listening");
                Socket cs = ss.accept();
                System.out.println("connected");
                ClientThread ct = new ClientThread(cs, clients);

                clients.add(ct);
                ct.start();

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        int portNo = 5555;
        if (args.length > 0) {
            portNo = Integer.parseInt(args[0]);
        }
        Server server;
        try {
            server = new Server(portNo);
            server.start();
        } catch (IOException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server can not start.");
        }

    }

}

class ClientThread extends Thread {

    private Socket sock;
    private ObjectInputStream sockIn;
    private ObjectOutputStream sockOut;
    private InputStream is;
    private OutputStream os;

    private final int login = 1, signup = 0, msgtype = 2, file = 3, activeFriend = 4, activeAnonymous = 5, inactiveFriends = 6;
    private final int singleActiveFriend = 7, singleAnonymous = 8, logout = 9, frequest = 10, prevMsg = 11, accepted = 12;
    private final int rejected = 13, offline = 14,fileReached = 15;

    // private BufferedReader br;
    private String userName = null;
    private File usersFile;
    private ArrayList<ClientThread> clients;
    //private ArrayList<String > inactivefriendlist = new ArrayList<String>();
    private ArrayList<ClientThread> activeFriendClient = new ArrayList<ClientThread>();
    private ArrayList<ClientThread> anonymousClient = new ArrayList<ClientThread>();

    public ClientThread(Socket s, ArrayList<ClientThread> cl) {
        clients = cl;

        sock = s;
        try {
            // System.out.println("read write");

            os = sock.getOutputStream();
            sockOut = new ObjectOutputStream(os);
            is = sock.getInputStream();
            sockIn = new ObjectInputStream(is);

            //sockOut.writeObject("thank you");
            //String str = (String )sockIn.readObject();
            // System.out.println(str);
        } catch (Exception ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean checkExistenceOfUser(String msg, int type) {
        boolean exst = false;
        BufferedReader br = null;
        try {

            br = new BufferedReader(new FileReader(usersFile));
            String user;
            String line = br.readLine();

            while (!exst && line != null) {
                if (type == signup) {
                    String[] words = line.split(" ");
                    user = words[0];
                } else {
                    user = line;
                }
                if (user.equals(msg)) {
                    //sockOut.writeObject(new ChatMessage(login,"success"));
                    exst = true;
                } else {
                    line = br.readLine();

                }

            }
            br.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return exst;
    }

    public synchronized void addUser(String str) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(usersFile, true)));
            pw.println(str);
            pw.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void addNewFriend(ChatMessage msg) {
        PrintWriter pw;
        try {
            File file = new File("FriendList/" + msg.getSource() + ".txt");
            if (!file.exists()) {
                file.createNewFile();
                pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            } else {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            }

            pw.println(msg.getDest());

            file = new File("FriendList/" + msg.getDest() + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            pw.println(msg.getSource());
            pw.close();

        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendAllMessage(ChatMessage msg) throws IOException {
        String fileName;
        int fn = userName.compareTo(msg.getMessage());
        if (fn < 0) {
            fileName = userName + msg.getMessage() + ".txt";
        } else {
            fileName = msg.getMessage() + userName + ".txt";
        }
        File nwfile = new File("AllMessage/" + fileName);
        if (nwfile.exists()) {
            StringBuilder msgg = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(nwfile));

                String line = br.readLine();
                while (line != null) {
                    msgg.append(line);
                    msgg.append("\n");
                    line = br.readLine();
                }
                br.close();

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            sockOut.writeObject(new ChatMessage(prevMsg, msgg.toString()));
        } else {
            sockOut.writeObject(new ChatMessage(prevMsg, ""));

        }
    }

    public synchronized void sendMessage(ChatMessage msg) {

        String dest = msg.getDest();

        String fileName;
        int fn = userName.compareTo(dest);
        if (fn < 0) {
            fileName = userName + dest + ".txt";
        } else {
            fileName = dest + userName + ".txt";
        }
        File nwfile = new File("AllMessage/" + fileName);
        PrintWriter pw;
        try {
            if (!nwfile.exists()) {

                nwfile.createNewFile();
                pw = new PrintWriter(new BufferedWriter(new FileWriter(nwfile)));

            } else {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(nwfile, true)));
            }

            pw.println(msg.getSource() + " :\n" + msg.getMessage() + "\n");
            pw.close();

        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        boolean sent = false;
        for (int i = 0; i < clients.size(); i++) {
            ClientThread clt = clients.get(i);
            if (clt.userName.equals(dest)) {

                try {
                    clt.sockOut.writeObject(msg);
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                sent = true;
                break;
            }
        }
        if (!sent) {
            nwfile = new File("Pending/" + dest + ".txt");
            if (!nwfile.exists()) {
                try {
                    nwfile.createNewFile();
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nwfile));
                    out.writeObject(msg);
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    ObjectOutputStream os2 = new ObjectOutputStream(new FileOutputStream(nwfile, true)) {
                        protected void writeStreamHeader() throws IOException {
                            reset();
                        }
                    };
                    os2.writeObject(msg);
                    os2.close();

                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /*public void sendPendingFile(ChatMessage msg) {

        int count;
        byte[] buffer = new byte[1024];
        File filetosend = new File("PendingFile/" + msg.getDest() + msg.getSource() + msg.getMessage());
        BufferedInputStream br;

        try {
            br = new BufferedInputStream(new FileInputStream(filetosend));

            sockOut.writeObject(msg);
            while ((count = br.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
                os.flush();
            }
            br.close();
            filetosend.delete();
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI2.class.getName()).log(Level.SEVERE, null, ex);
        }

    }*/
    public synchronized void sendFile(ChatMessage msg) {
        boolean sent = false;
        for (int i = 0; i < clients.size(); i++) {
            ClientThread clt = clients.get(i);
            if (clt.userName != null && clt.userName.equals(msg.getDest())) {

                try {
                    /*int count;
                    byte[] buffer = new byte[1024];

                    clt.sockOut.writeObject(msg);
                    while ((count = is.read(buffer)) >= 0) {
                    clt.os.write(buffer, 0, count);
                    clt.os.flush();
                    }*/
                    clt.sockOut.writeObject(msg);
                } catch (IOException ex) {
                    //Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                    //ignore
                }
                System.out.println("file sent from server");

                sent = true;
                break;
            }
        }
        if (!sent) {
            File nwfile = new File("Pending/" + msg.getDest() + ".txt");
            if (!nwfile.exists()) {

                try {
                    nwfile.createNewFile();
                    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nwfile));
                    out.writeObject(msg);
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {

                try {
                    ObjectOutputStream os2 = new ObjectOutputStream(new FileOutputStream(nwfile, true)) {
                        protected void writeStreamHeader() throws IOException {
                            reset();
                        }
                    };
                    os2.writeObject(msg);
                    os2.close();
                    //PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(nwfile,true)));
                    // pw.println("$sender$\n"+msg.getSource());
                    //pw.println("$type$\n"+msg.getType());
                    //pw.println("$msg$\n"+msg.getMessage());

                    //pw.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {

                }

            }
            /* nwfile = new File("PendingFile/"+msg.getDest()+msg.getSource()+msg.getMessage());
            
            BufferedOutputStream bout;

        try {
            nwfile.createNewFile();
            bout = new BufferedOutputStream(new FileOutputStream(nwfile));
            int count;
            byte[] buffer = new byte[1024];
            //sockOutput.writeObject(new ChatMessage(file,filetosend.getName(),userName,friendNameLabel.getText()));
            while ((count = is.read(buffer)) >= 0) {
                bout.write(buffer, 0, count);
                bout.flush();
                 System.out.println(buffer);
            }
             bout.close();
             System.out.println("pending file sent from server");
        } catch (IOException ex) {
            Logger.getLogger(ClientGUI2.class.getName()).log(Level.SEVERE, null, ex);
        }
             */
        }
    }

    public void sendActiveList() throws IOException {
        ArrayList<String> inactivefriendlist = new ArrayList<String>();
        ArrayList<String> activefriendlist = new ArrayList<String>();
        ArrayList<String> anonymousList = new ArrayList<String>();
        File fl = new File("FriendList/" + userName + ".txt");
        if (!fl.exists()) {
            try {
                fl.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(fl));
            String fr;
            while ((fr = br.readLine()) != null) {
                inactivefriendlist.add(fr);
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < clients.size(); i++) {
            String s = clients.get(i).userName;
            if (s != null && !s.equals(userName)) {
                if (inactivefriendlist.remove(s)) {
                    activefriendlist.add(s);
                    activeFriendClient.add(clients.get(i));
                } else {
                    anonymousList.add(s);
                    anonymousClient.add(clients.get(i));
                }
            }
        }

        sockOut.writeObject(new ChatMessage(activeFriend, activefriendlist));
        sockOut.writeObject(new ChatMessage(inactiveFriends, inactivefriendlist));
        sockOut.writeObject(new ChatMessage(activeAnonymous, anonymousList));

    }

    public void initialize() throws IOException {
        sendActiveList();

        for (int i = 0; i < activeFriendClient.size(); i++) {
            try {
                activeFriendClient.get(i).sockOut.writeObject(new ChatMessage(userName, singleActiveFriend));
            } catch (IOException ex) {
                //ignore
            }
        }
        for (int i = 0; i < anonymousClient.size(); i++) {
            try {

                anonymousClient.get(i).sockOut.writeObject(new ChatMessage(userName, singleAnonymous));
            } catch (IOException ex) {
                //ignore
            }
        }

        File nwfile = new File("Pending/" + userName + ".txt");
        if (nwfile.exists()) {
            ObjectInputStream inp = null;
            try {
                inp = new ObjectInputStream(new FileInputStream(nwfile));
                ChatMessage msg;//= (ChatMessage) inp.readObject();
                while (true) {
                    msg = (ChatMessage) inp.readObject();
                    try {
                        sockOut.writeObject(msg);
                    } catch (IOException ex) {
                        throw ex;
                    }

                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EOFException eof) {
                //ignored
            } catch (IOException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (inp != null) {
                        inp.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                nwfile.delete();
                System.out.println("pending mesg sent");
            }
        }

    }

    public void run() {

        ChatMessage msg;
        usersFile = new File("users.txt");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        while (true) {

            try {
                msg = (ChatMessage) sockIn.readObject();

                // System.out.println(msg.getMessage());
                switch (msg.getType()) {
                    case login:

                        boolean exst = checkExistenceOfUser(msg.getMessage(), login);
                        if (exst) {

                            sockOut.writeObject(new ChatMessage(login, "success"));
                            String[] strs = msg.getMessage().split(" ");
                            userName = strs[0];
                            initialize();

                        } else {

                            sockOut.writeObject(new ChatMessage(login, "failed"));

                        }

                        break;
                    case signup:
                        String[] str = msg.getMessage().split(" ");
                        System.out.println(str[0]);
                        if (str.length > 2) {

                            sockOut.writeObject(new ChatMessage(signup, "failed"));

                            break;
                        }
                        exst = checkExistenceOfUser(str[0], signup);
                        if (exst) {

                            sockOut.writeObject(new ChatMessage(signup, "failed"));

                        } else {

                            addUser((msg.getMessage()));
                            System.out.println("User Added");

                            sockOut.writeObject(new ChatMessage(signup, "success"));
                            userName = str[0];
                            initialize();

                        }

                        break;

                    case msgtype:
                    case fileReached:
                        sendMessage(msg);
                        
                        break;
                    case prevMsg:
                        sendAllMessage(msg);
                        break;
                    case frequest:

                    case rejected:
                        sendFriendRequest(msg);

                        break;
                    case accepted:
                        addNewFriend(msg);
                        sendFriendRequest(msg);
                        break;
                    case file:
                    
                        sendFile(msg);
                        break;
                    case logout:
                        logout();
                        sockOut.writeObject(new ChatMessage(logout));
                        userName = null;
                        break;
                    
                    

                }
            } catch (IOException ex) {
                //Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("user disconnected");
                break;
                //log out user if he is logged in
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        try {
            System.out.println("cut oof");
            logout();
            clients.remove(this);
            if (sockIn != null) {
                sockIn.close();
            }

            if (sockOut != null) {
                sockOut.close();
            }
            if (sock.isConnected()) {
                sock.close();
            }

        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void logout() {
        ClientThread user;
        for (int i = 0; i < clients.size(); i++) {
            user = clients.get(i);
            if (user.userName != null && !user.userName.equals(userName)) {
                try {
                    user.sockOut.writeObject(new ChatMessage(offline, "", userName, ""));
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void sendFriendRequest(ChatMessage msg) {

        for (int i = 0; i < clients.size(); i++) {
            ClientThread clt = clients.get(i);
            if (clt.userName.equals(msg.getDest())) {
                try {
                    clt.sockOut.writeObject(msg);
                    System.out.println("req sent from server");
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }

                break;
            }
        }
    }

}
