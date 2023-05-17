import java.sql.*;

public class Client extends Account{
    private String surname;
    private String name;
    private String state;

    private Search_criteria search_criteria;

    public Client() {
        search_criteria = new Search_criteria();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Search_criteria getSearch_criteria() {
        return search_criteria;
    }

    public void setSearch_criteria(Search_criteria search_criteria) {
        this.search_criteria = search_criteria;
    }
    @Override
    public void insertInfoDataBase(String url, String username, String password){
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = String.format("INSERT INTO public.client(account_idaccount, surname, name, state) VALUES(%s, '%s', '%s', '%s');", getId_account(), surname, name, state);
            try{
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                sql = "INSERT INTO public.search_criteria(account_idaccount)VALUES ("+ getId_account()+");";
                statement.executeUpdate(sql);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }}
        catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();}
    }


    public String getChanges_c(Client account2) {
        String result = "";
        if(!surname.equals(account2.surname)){
            result += "surname = '" + account2.surname + "',";
        }
        if(!name.equals(account2.name)){
            result += "name = '" + account2.name + "',";
        }
        if(!state.equals(account2.state)){
            result += "state = '" + account2.state + "',";
        }

        return result.substring(0, result.length() - 1);
    }

}
