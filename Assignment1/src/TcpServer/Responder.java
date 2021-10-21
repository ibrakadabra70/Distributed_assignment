package TcpServer;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Responder implements Runnable {
    public Socket s;

    public Responder(Socket s){
        this.s = s;
    }
    public void run() {

        try {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            DataInputStream in = new DataInputStream(s.getInputStream());
            //keep running until finish command is given
            while (true){
                String cmd = in.readUTF();
                System.out.println(cmd);
                //if finish command is received the socket connection will be closed
                if(cmd.equals("finish")){
                    //Sends Exiting message
                    out.writeUTF("Connection Closed");
                    System.out.println("A connection is closed");
                    out.flush();
                    out.close();
                    in.close();
                    s.close();
                    break;
                    //this if statement handles get request
                }else if(cmd.startsWith("get ")){
                    //get file name
                    String filename = cmd.replaceFirst("get ", "");
                    File f = new File(filename);
                    //checks if file exist in the root directory
                    if(f.exists()){
                        //if file exists reads all the content and sends to client
                        String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                        content = "200 OK\n\n" + content;
                        out.writeUTF(content);
                        out.flush();
                    }else {
                        //sends that file is not found
                        out.writeUTF("Error 404 File not found");
                    }
                //this part handles info commands
                }else if(cmd.equals("info")){
                    out.writeUTF("Host name:"+ InetAddress.getLocalHost ()+" connection number:"+ Server.counter);
                }else if(cmd.startsWith("evaluate ")){
                    String exp = cmd.replaceFirst("evaluate ","");
                    out.writeUTF(Double.toString(eval(exp)));
                    out.flush();
                }else if(cmd.equals("help")){
                    out.writeUTF("Usable commands are: \ninfo: Get info about server" +
                            "\nget <filename>: To get the content of the file in server" +
                            "\nevaluate <math expression>: Server calculates the expression and returns result" +
                            "\nfinish: Client closes and also server stops the thread assigned to client");
                }
                else {
                    out.writeUTF("Invalid Request");
                }
            }


            out.close();
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

}