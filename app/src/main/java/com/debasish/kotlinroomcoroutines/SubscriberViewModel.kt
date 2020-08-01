package com.debasish.kotlinroomcoroutines

import android.os.Message
import android.util.Patterns
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debasish.kotlinroomcoroutines.db.Event
import com.debasish.kotlinroomcoroutines.db.Subscriber
import com.debasish.kotlinroomcoroutines.db.SubscriberRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel(),Observable {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete: Subscriber

    @Bindable
    val inputName = MutableLiveData<String>()

    @Bindable
    val inputEmail = MutableLiveData<String>()

    @Bindable
    val saveOrUpdateButtonText = MutableLiveData<String>()

    @Bindable
    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()

    val message: LiveData<Event<String>>
        get() = statusMessage

    init {
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun saveOrUpdate() {
        if(inputName.value == null){
            statusMessage.value = Event("Please Enter Subscribers name")
        } else if (inputEmail.value == null){
            statusMessage.value = Event("Please Enter Subscribers email")
        } else if(!Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches()){
            statusMessage.value = Event("Please Enter correct email address")
        } else {
            if(isUpdateOrDelete){
                subscriberToUpdateOrDelete.name = inputName.value!!
                subscriberToUpdateOrDelete.email = inputEmail.value!!
                update(subscriberToUpdateOrDelete)
            } else {
                val name: String =inputName.value!!
                val email: String = inputEmail.value!!
                insert(Subscriber(0,name,email))
                inputName.value = null
                inputEmail.value = null
            }

        }

    }

    fun clearAllOrDelete() {
        if(isUpdateOrDelete){
            delete(subscriberToUpdateOrDelete)
        } else {
            clearAll()
        }

    }

    fun insert(subscriber: Subscriber) : Job = viewModelScope.launch {
        val newRowId: Long = repository.insert(subscriber)
        if(newRowId > -1){
            statusMessage.value = Event("Subscriber inserted sucessfully")
        } else {
            statusMessage.value = Event("Error Occured")
        }

    }

    fun update(subscriber: Subscriber):Job = viewModelScope.launch {
        val noOfRows = repository.update(subscriber)
        if(noOfRows > 0){
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            saveOrUpdateButtonText.value = "Save"
            clearAllOrDeleteButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRows updated sucessfully")

        } else {
            statusMessage.value = Event("Error Occured")
        }

    }

    fun delete(subscriber: Subscriber): Job = viewModelScope.launch {
        val noOfRowsDeleted = repository.delete(subscriber)
        if(noOfRowsDeleted > 0){
            inputName.value = null
            inputEmail.value = null
            isUpdateOrDelete = false
            saveOrUpdateButtonText.value = "Save"
            clearAllOrDeleteButtonText.value = "Clear All"
            statusMessage.value = Event("$noOfRowsDeleted deleted sucessfully")

        } else {

        }

    }

    fun clearAll() : Job =viewModelScope.launch {
        val  noOfRowsDeleted = repository.deleteAll()
        if(noOfRowsDeleted > 0){
            statusMessage.value = Event("$noOfRowsDeleted Deleted sucessfully")
        } else {
            statusMessage.value = Event("Error Occured")
        }

    }

    fun initUpdateAndDelete(subscriber: Subscriber) {
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveOrUpdateButtonText.value = "Update"
        clearAllOrDeleteButtonText.value = "Delete"

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}