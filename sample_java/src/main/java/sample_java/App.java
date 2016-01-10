package sample_java;

import java.io.File;
import java.util.List;

import io.rx_cache.Reply;
import rx.Subscriber;
import rx.functions.Action1;
import sample_data.Repository;
import sample_data.entities.User;

public class App {
    private int idLastUserQueried = 1;
    private boolean close, isInMainMenu, isInUsers, isInProfile;
    private final Repository repository;
    private final static int USERS = 0, PROFILE = 1, CLOSE = 2,
            SHOW_NEXT_PAGE = 0, RETURN_FIRST_PAGE = 1, RETURN_FIRST_PAGE_AND_CLEAR_CACHE_PAGE = 2, BACK = 3,
            SHOW_CURRENT_USER = 0, LOG_IN_USER_RANDOM_USER = 1, LOG_OUT_USER = 2;

    public App() {
        File cacheDir = new File(System.getProperty("user.home"), "Desktop");
        repository = new Repository(cacheDir);
        isInMainMenu = true;
    }

    public void printInstructionsForAvailableOptions() {
        if (isInMainMenu) {
            System.out.println("#0 Users - #1 Profile - #2 Close");
        } else if(isInUsers) {
            System.out.println("#0 Show Next Page - #1 Return first page - #2 Return and clear page - #3 Back");
        } else if(isInProfile) {
            System.out.println("#0 Show Current User - #1 Login random user - #2 Logout user - #3 Back");
        }
    }

    public void processNewInput(int input) {
        if (isInMainMenu) {
            if (input == CLOSE) close = true;
            else if(input == USERS) {
                isInMainMenu = false;
                isInUsers = true;
                isInProfile = false;
            } else if(input == PROFILE) {
                isInMainMenu = false;
                isInUsers = false;
                isInProfile = true;
            }
        } else if(isInUsers) {
            if (input == BACK) isInMainMenu = true;
            else if(input == SHOW_NEXT_PAGE) printUsers(false);
            else if(input == RETURN_FIRST_PAGE) {
                idLastUserQueried = 1;
                printUsers(false);
            } else if(input == RETURN_FIRST_PAGE_AND_CLEAR_CACHE_PAGE) {
                idLastUserQueried = 1;
                printUsers(true);
            }
        } else if(isInProfile) {
            if (input == BACK) isInMainMenu = true;
            else if(input == SHOW_CURRENT_USER) showCurrentUser();
            else if(input == LOG_IN_USER_RANDOM_USER) loginRandomUser();
            else if(input == LOG_OUT_USER) logoutUser();
        }
    }

    public boolean close() {
        return close;
    }

    private void printUsers(boolean update) {
        repository.getUsers(idLastUserQueried, update).subscribe(new Action1<Reply<List<User>>>() {
            @Override public void call(Reply<List<User>> reply) {
                System.out.println("Source: " + reply.getSource().name());

                for (User user : reply.getData()) {
                    System.out.println(user);
                }

                idLastUserQueried = reply.getData().get(reply.getData().size()-1).getId();
            }
        });
    }

    private void showCurrentUser() {
        repository.getLoggedUser(false).subscribe(new Subscriber<Reply<User>>() {
            @Override public void onCompleted() {}

            @Override public void onError(Throwable e) {
                System.out.println(e.getCause());
            }

            @Override public void onNext(Reply<User> user) {
                System.out.println("Current user");
                System.out.println(user.getData());
            }
        });
    }

    private void loginRandomUser() {
        User user = new User(1, "Random", "Random Avatar");
        repository.loginUser(user.getLogin()).subscribe(new Action1<Reply<User>>() {
            @Override public void call(Reply<User> userReply) {
                System.out.println("User logged");
            }
        });
    }

    private void logoutUser() {
        repository.logoutUser().subscribe(new Subscriber<String>() {
            @Override public void onCompleted() {}

            @Override public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override public void onNext(String message) {
                System.out.println(message);
            }
        });
    }
}
