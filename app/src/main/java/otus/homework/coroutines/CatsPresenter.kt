package otus.homework.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CatsPresenter(
    private val catsService: CatsService,
    private val imageService: CatsImageService
) {

    private var _catsView: ICatsView? = null
    private val job = Job()
    private val presenterScope =
        CoroutineScope(Dispatchers.Main + CoroutineName(COROUTINE_NAME) + job)

    fun onInitComplete() {

        presenterScope.launch {
            try {
                coroutineScope {
                    val fact = async { catsService.getCatFact().fact }
                    val image = async { imageService.getCatImage().first() }
                    _catsView?.populate(
                        CatsData(
                            fact.await(),
                            image.await()
                        )
                    )
                }
            } catch (e: java.net.SocketTimeoutException) {
                _catsView?.showError(CatsView.SOCKET_TIMEOUT_EXCEPTION)
            } catch (e: Exception) {
                CrashMonitor.trackWarning()
                _catsView?.showError(CatsView.OTHER_EXCEPTION, e.message)
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        job.cancel()
        _catsView = null

    }

    companion object {
        const val COROUTINE_NAME = "CatsCoroutine"
    }
}
