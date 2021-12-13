package com.example.motobike

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.motobike.adapter.MyDrinkAdapter
import com.example.motobike.eventbus.UpdateCartEvent
import com.example.motobike.listener.ICartLoadListener
import com.example.motobike.listener.IDrinkLoadListener
import com.example.motobike.model.CartModel
import com.example.motobike.model.DrinkModel
import com.example.motobike.utils.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Modifier

class MainActivity : AppCompatActivity(), IDrinkLoadListener, ICartLoadListener {

    lateinit var drinkLoadListener: IDrinkLoadListener
    lateinit var cartLoadListener: ICartLoadListener

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register( this)

    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
            EventBus.getDefault().unregister(this)

    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onUpdateCartEvent(event:UpdateCartEvent){
        countCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        loadDrinkFromFirebase()
        countCartFromFirebase()

    }



    private fun countCartFromFirebase() {
        val cartModels :MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                  for (cartSnapshot in snapshot.children){
                      var cartModel= cartSnapshot.getValue(CartModel::class.java)
                      cartModel!!.key=cartSnapshot.key
                      cartModels.add(cartModel)
                  }
                    cartLoadListener.onLoadCardSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                  cartLoadListener.onLoadCartFailed(error.message)
                }
            })
    }

    private fun loadDrinkFromFirebase() {
        val drinkModels: MutableList<DrinkModel> =ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("MotoBike")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){

                        for (drinkSnapshot in snapshot.children)
                        {
                            var drinkModel =drinkSnapshot.getValue(DrinkModel::class.java)
                            drinkModel!!.key = drinkSnapshot.key
                            drinkModels.add(drinkModel)
                        }
                        drinkLoadListener.onDrinkloadSuccess(drinkModels)
                    }
                    else
                    {

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    drinkLoadListener.onDrinkloadFailed(error.message)
                }

            })
    }

    private fun init(){
        drinkLoadListener = this
        cartLoadListener = this
        val gridLayoutManger= GridLayoutManager(this,2)
        recycler_drink.layoutManager=gridLayoutManger
        recycler_drink.addItemDecoration(SpaceItemDecoration())

        btnCart.setOnClickListener{startActivity( Intent(this,CartActivity::class.java))}
    }

    override fun onDrinkloadSuccess(drinkModelList: List<DrinkModel>) {
        var adapter = MyDrinkAdapter(this,drinkModelList!!,cartLoadListener)
        recycler_drink.adapter= adapter
    }

    override fun onDrinkloadFailed(message: String?) {
        Snackbar.make(mainLayout,message!!, Snackbar.LENGTH_LONG).show()
    }

    override fun onLoadCardSuccess(cartModelList: List<CartModel>) {
        var cartSum=0
        for(cartModel in cartModelList!!) cartSum+= cartModel!!.quantity
        badge!!.setNumber(cartSum)
    }


    override fun onLoadCartFailed(message: String?) {
        Snackbar.make(mainLayout,message!!, Snackbar.LENGTH_LONG).show()
    }
}