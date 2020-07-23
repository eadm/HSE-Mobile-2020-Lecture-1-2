package ru.nobird.android.myapplication.arch.view.movie.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.activity_view_model.*
import ru.nobird.android.myapplication.R
import ru.nobird.android.myapplication.arch.domain.movie.model.MovieData
import ru.nobird.android.myapplication.arch.presentation.movie.MovieViewModel
import ru.nobird.android.myapplication.arch.view.movie.ui.dialog.CreateMovieDialogFragment
import ru.nobird.android.myapplication.arch.presentation.movie.model.State
import ru.nobird.android.myapplication.arch.view.movie.ui.adapter.MovieAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class ViewModelActivity : AppCompatActivity(),
    CreateMovieDialogFragment.Callback {

    /**
     * todo: использовать ViewModelProvider.Factory для создания MovieViewModel,
     * todo: внутри которой будут разрешены зависимости MovieViewModel
     * todo: https://medium.com/koderlabs/viewmodel-with-viewmodelprovider-factory-the-creator-of-viewmodel-8fabfec1aa4f
     */

    /**
     * todo 2: сделать тоже самое через Dagger и DaggerViewModelFactory
     */
    private val viewModel: MovieViewModel by viewModels()

    private val viewStateDelegate = ViewStateDelegate<State>()
    private val adapter = DefaultDelegateAdapter<MovieData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_model)

        supportActionBar?.apply {
            title = getString(R.string.topic_network)
            setDisplayHomeAsUpEnabled(true)
        }

        viewStateDelegate.addState<State.Idle>()
        viewStateDelegate.addState<State.Loading>(progress)
        viewStateDelegate.addState<State.Data>(recycler, addItem, clear)
        viewStateDelegate.addState<State.Error>(errorText, errorAction)

        addItem.setOnClickListener {
            CreateMovieDialogFragment.newInstance()
                .showIfNotExists(supportFragmentManager, CreateMovieDialogFragment.TAG)
        }
        clear.setOnClickListener { viewModel.onDeleteLastItem() }
        errorAction.setOnClickListener { viewModel.fetchItems() }

        adapter += MovieAdapterDelegate()

        recycler.adapter = adapter

        viewModel.state.observe(this, ::setState)
    }

    private fun setState(state: State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is State.Data ->
                adapter.items = state.movies
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onCreateMovie(name: String) {
        viewModel.onCreateMovie(name)
    }
}