import java.sql.*;

public abstract class Account implements Cloneable{
    private Long id_account;
    private String email;
    private String password;

    public Long getId_account() {
        return id_account;
    }

    public void setId_account(Long id_account) {
        this.id_account = id_account;
    }

    public void updateId_account(String url, String username, String password, Long id, boolean id_account_null){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = String.format("UPDATE public.account_now SET account_idaccount=%s WHERE id_user='%s';", (id_account_null)?" NULL":id_account, id);
            try{
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }

    public static void updateId_account(String url, String username, String password, Long id_account, Long id){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = String.format("UPDATE public.account_now SET account_idaccount=%s WHERE id_user='%s';", (id==null)?" NULL":id_account, id);
            try{
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChanges_ac(Account account2) {
        String result = "";
        if(!password.equals(account2.getPassword())){
            result += "password_a = '" + MailData.encrypt(account2.password) +"'";
        }
        return result;
    }


    public void insertAccountDataBase(String url, String username, String password, String role){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = String.format("INSERT INTO public.account(idaccount, email, password_a, role) VALUES (%s, '%s', '%s', '%s');", id_account, email, MailData.encrypt(this.password), role);
            try{
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }

    public abstract void insertInfoDataBase(String url, String username, String password);


    @Override
    public Account clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (Account) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
