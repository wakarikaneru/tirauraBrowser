package studio.wakaru.test2.ui.tubuyaki;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TubuyakiViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TubuyakiViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}