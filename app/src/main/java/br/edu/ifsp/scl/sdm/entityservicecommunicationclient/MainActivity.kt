package br.edu.ifsp.scl.sdm.entityservicecommunicationclient

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.scl.sdm.entityservicecommunication.IncrementBoundServiceInterface
import br.edu.ifsp.scl.sdm.entityservicecommunicationclient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater) // infla o layout
    }

    private lateinit var incrementBoundServiceIntent: Intent // intent para iniciar o serviço
    private var counter = 0 // variável de controle do contador

    // private lateinit var ibsMessenger: Messenger // messenger do serviço
    private var ibService: IncrementBoundServiceInterface? = null


    // Conexão com o serviço
    private val incrementBoundServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v(getString(R.string.app_name), "Client bound to the service.")

            service?.also { // verifica se o serviço existe

                ibService = IncrementBoundServiceInterface.Stub.asInterface(service)

                /*
                ibsMessenger = Messenger(service)

                // envia uma mensagem para o serviço
                ibsMessenger.send(Message.obtain().apply {

                    Messenger(object : Handler(Looper.myLooper()!!) {

                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            counter = msg.data.getInt("VALUE")
                            Toast.makeText(
                                this@MainActivity,
                                "You clicked $counter times",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }).also { messenger ->
                        replyTo = messenger
                    }
                })
                */
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v(getString(R.string.app_name), "Client unbound to the service.")
            ibService = null
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)

        incrementBoundServiceIntent = Intent().apply {
            component = ComponentName(
                "br.edu.ifsp.scl.sdm.entityservicecommunication",
                "br.edu.ifsp.scl.sdm.entityservicecommunication.IncrementBoundService"
            )
        }
        if (!bindService(
                incrementBoundServiceIntent,
                incrementBoundServiceConnection,
                BIND_AUTO_CREATE
            )
        ) {
            Toast.makeText(this, "Service unavailable", Toast.LENGTH_SHORT).show()
            finish()
        }

        with(amb) {
            mainTb.apply {
                getString(R.string.app_name).also { setTitle(it) }
                setSupportActionBar(this)
            }
            incrementBt.setOnClickListener {

                Thread {
                    ibService?.increment(counter)?.also {
                        counter = it
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "You clicked $counter times",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()

                /*
                ibsMessenger.send(Message.obtain().apply {
                    data.putInt("VALUE", counter)
                })
                */
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(incrementBoundServiceConnection)
    }
}