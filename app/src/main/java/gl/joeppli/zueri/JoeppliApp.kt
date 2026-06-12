package gl.joeppli.zueri

import android.app.Application
import gl.joeppli.zueri.data.RecyclingRepository

class JoeppliApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RecyclingRepository.initialize(this)
    }
}
