package org.sample

import android.os.Bundle
import masmini.android.FluxActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import masmini.LoggerInterceptor
import masmini.MasMiniGen
import org.sample.databinding.HomeActivityBinding

class SampleActivity : FluxActivity() {

    private val dispatcher = MasMiniGen.newDispatcher()
    private val dummyStore = DummyStore()

    private lateinit var binding: HomeActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val stores = listOf(dummyStore)
        MasMiniGen.subscribe(dispatcher, stores).track()
        stores.forEach { it.initialize() }

        dummyStore.subscribe {
            binding.demoText.text = it.text
        }

        Grove.plant(ConsoleLogTree())
        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        dispatcher.dispatch(ActionTwo("2"))
    }
}