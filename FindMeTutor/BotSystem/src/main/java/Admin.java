public class Admin extends Account{

    private Search_criteria search_criteria;
    public Admin() {
        search_criteria =new Search_criteria();
    }

    public Search_criteria getSearch_criteria() {
        return search_criteria;
    }

    public void setSearch_criteria(Search_criteria search_criteria) {
        this.search_criteria = search_criteria;
    }

    @Override
    public void insertInfoDataBase(String url, String username, String password) {
    }


}
