package io.eugenethedev.taigamobile.ui.screens.projectselector

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.Session
import io.eugenethedev.taigamobile.TaigaApp
import io.eugenethedev.taigamobile.domain.entities.ProjectInSearch
import io.eugenethedev.taigamobile.domain.repositories.ISearchRepository
import io.eugenethedev.taigamobile.ui.utils.MutableLiveResult
import io.eugenethedev.taigamobile.ui.utils.Result
import io.eugenethedev.taigamobile.ui.utils.ResultStatus
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ProjectSelectorViewModel : ViewModel() {

    @Inject lateinit var searchRepository: ISearchRepository
    @Inject lateinit var session: Session

    val projects = MutableLiveResult<List<ProjectInSearch>>()
    val isProjectSelected = MutableLiveData(false)

    init {
        TaigaApp.appComponent.inject(this)
    }

    private var currentPage = 0
    private var maxPage = Int.MAX_VALUE
    private var currentQuery = ""

    fun start() {
        projects.value = Result(ResultStatus.SUCCESS, emptyList())
        isProjectSelected.value = false
        currentPage = 0
        maxPage = Int.MAX_VALUE
        loadData()
    }

    fun selectProject(project: ProjectInSearch) {
        session.apply {
            currentProjectId = project.id
            currentProjectName = project.name
        }
        isProjectSelected.value = true
    }

    fun loadData(query: String = "") = viewModelScope.launch {
        query.toLowerCase(Locale.getDefault()).takeIf { it != currentQuery }?.let {
            currentQuery = it
            currentPage = 0
            maxPage = Int.MAX_VALUE
            projects.value = Result(ResultStatus.SUCCESS, emptyList())
        }

        if (currentPage == maxPage) return@launch

        projects.value = Result(ResultStatus.LOADING, projects.value?.data)
        try {
            searchRepository.searchProjects(query, ++currentPage)
                .also { projects.value = Result(ResultStatus.SUCCESS, projects.value?.data.orEmpty() + it) }
                .takeIf { it.isEmpty() }
                ?.run { maxPage = currentPage /* reached maximum page */ }
        } catch (e: Exception) {
            Timber.w(e)
            projects.value = Result(ResultStatus.ERROR, projects.value?.data, message = R.string.common_error_message)
        }
    }
}