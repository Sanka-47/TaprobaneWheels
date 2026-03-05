package lk.jiat.eshop.listener;

public interface FirestoreCallback<T> {
    void onCallback(T data);
}
