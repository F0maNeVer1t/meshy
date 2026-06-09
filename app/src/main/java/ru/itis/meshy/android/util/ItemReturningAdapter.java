package ru.itis.meshy.android.util;

public interface ItemReturningAdapter<I> {

	I getItemAt(int position);

	int getItemCount();

}
