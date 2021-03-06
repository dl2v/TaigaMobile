package io.eugenethedev.taigamobile.ui.screens.projectselector

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.ProjectInSearch
import io.eugenethedev.taigamobile.ui.components.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.components.ContainerBox
import io.eugenethedev.taigamobile.ui.components.Loader
import io.eugenethedev.taigamobile.ui.components.SlideAnimView
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.utils.ResultStatus
import io.eugenethedev.taigamobile.ui.utils.subscribeOnError

@Composable
fun ProjectSelectorScreen(
    navController: NavController,
    onError: @Composable (message: Int) -> Unit = {},
) {
    val viewModel: ProjectSelectorViewModel = viewModel()
    remember {
        viewModel.start()
        null
    }

    val projects by viewModel.projects.observeAsState()
    projects?.subscribeOnError(onError)
    val isProjectSelected by viewModel.isProjectSelected.observeAsState()

    var queryInput by remember { mutableStateOf(TextFieldValue()) }

    SlideAnimView(navigateBack = navController::popBackStack) {
        if (isProjectSelected!!) {
            it()
        }

        ProjectSelectorScreenContent(
            projects = projects?.data.orEmpty(),
            navigateBack = it,
            isLoading = projects?.resultStatus == ResultStatus.LOADING,
            query = queryInput,
            onQueryChanged = { queryInput = it },
            loadData = { viewModel.loadData(queryInput.text) },
            selectProject = viewModel::selectProject
        )
    }
}


@Composable
fun ProjectSelectorScreenContent(
    projects: List<ProjectInSearch>,
    isLoading: Boolean = false,
    query: TextFieldValue = TextFieldValue(),
    onQueryChanged: (TextFieldValue) -> Unit = {},
    navigateBack: () -> Unit = {},
    loadData: () -> Unit = {},
    selectProject: (ProjectInSearch) -> Unit  = {}
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    AppBarWithBackButton(
        title = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_projects_hint),
                        style = MaterialTheme.typography.body1,
                        color = Color.Gray
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.wrapContentHeight()
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.body1.merge(TextStyle(color = MaterialTheme.colors.onSurface)),
                    cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { loadData() })
                )
            }
        },
        navigateBack = navigateBack
    )

    if (isLoading && projects.isEmpty()) {
        Loader()
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(projects.toList()) { index, item ->
            ItemProject(
                project = item,
                onClick = { selectProject(item) }
            )

            if (index < projects.size - 1) {
                Divider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.LightGray
                )
            }

            if (index == projects.lastIndex) {
                if (isLoading) {
                    Loader()
                }

                Spacer(Modifier.height(6.dp))

                SideEffect {
                    loadData()
                }
            }
        }
    }
}

@Composable
private fun ItemProject(
    project: ProjectInSearch,
    onClick: () -> Unit = {}
) = ContainerBox(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    Column {

        project.takeIf { it.isMember || it.isAdmin || it.isOwner  }?.let {
            Text(
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.primary,
                text = stringResource(
                    when {
                        project.isOwner -> R.string.project_owner
                        project.isAdmin -> R.string.project_admin
                        project.isMember -> R.string.project_member
                        else -> 0
                    }
                )
            )
        }

        Text(
            text = stringResource(R.string.project_name_template).format(project.name, project.slug),
            style = MaterialTheme.typography.body1,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectSelectorScreenPreview() = TaigaMobileTheme {
    ProjectSelectorScreenContent(
        listOf(
            ProjectInSearch(0, "Cool", "slug",false, false, false),
            ProjectInSearch(1, "Cooler", "slug", true, false, false)
        )
    )
}
