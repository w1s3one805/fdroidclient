package org.fdroid.fdroid.views.appdetails

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.LocaleListCompat
import androidx.core.util.Consumer
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.fdroid.database.Repository
import org.fdroid.fdroid.R
import org.fdroid.fdroid.Utils
import org.fdroid.fdroid.compose.ComposeUtils.FDroidContent
import org.fdroid.fdroid.compose.ComposeUtils.FDroidOutlineButton
import org.fdroid.index.IndexFormatVersion.TWO

/**
 * A helper method to show [RepoChooser] from Java code.
 */
fun setContentRepoChooser(
    composeView: ComposeView,
    repos: List<Repository>,
    currentRepoId: Long,
    preferredRepoId: Long,
    onRepoChanged: Consumer<Repository>,
    onPreferredRepoChanged: Consumer<Long>,
) {
    composeView.setContent {
        FDroidContent {
            RepoChooser(
                repos = repos,
                currentRepoId = currentRepoId,
                preferredRepoId = preferredRepoId,
                onRepoChanged = onRepoChanged::accept,
                onPreferredRepoChanged = onPreferredRepoChanged::accept,
                modifier = Modifier.background(MaterialTheme.colors.surface),
            )
        }
    }
}

@Composable
fun RepoChooser(
    repos: List<Repository>,
    currentRepoId: Long,
    preferredRepoId: Long,
    onRepoChanged: (Repository) -> Unit,
    onPreferredRepoChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (repos.isEmpty()) {
        // no-op should not happen
    } else if (repos.size == 1) {
        RepoItem(
            repo = repos[0],
            isPreferred = false, // don't show "preferred" if the only repo anyway
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        RepoDropDown(
            repos = repos,
            currentRepoId = currentRepoId,
            preferredRepoId = preferredRepoId,
            onRepoChanged = onRepoChanged,
            onPreferredRepoChanged = onPreferredRepoChanged,
            modifier = modifier,
        )
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun RepoDropDown(
    repos: List<Repository>,
    currentRepoId: Long,
    preferredRepoId: Long,
    onRepoChanged: (Repository) -> Unit,
    onPreferredRepoChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentRepo = repos.find { it.repoId == currentRepoId }
        ?: error("Current repoId not in list")
    val localeList = LocaleListCompat.getDefault()
    val res = LocalContext.current.resources

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box {
            OutlinedTextField(
                value = TextFieldValue(buildAnnotatedString {
                    append(currentRepo.getName(localeList) ?: "Unknown Repository")
                    if (currentRepo.repoId == preferredRepoId) {
                        append(" ")
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append("★ ")
                        append(stringResource(R.string.app_details_repository_preferred))
                    }
                }),
                textStyle = MaterialTheme.typography.body2,
                onValueChange = {},
                label = {
                    Text(stringResource(R.string.app_details_repositories))
                },
                leadingIcon = {
                    if (LocalInspectionMode.current) Image(
                        painter = rememberDrawablePainter(
                            ResourcesCompat.getDrawable(res, R.drawable.ic_launcher, null)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    ) else GlideImage(
                        model = Utils.getDownloadRequest(
                            currentRepo,
                            currentRepo.getIcon(localeList)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    ) {
                        it.fallback(R.drawable.ic_repo_app_default)
                            .error(R.drawable.ic_repo_app_default)
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.app_details_repository_expand),
                    )
                },
                singleLine = true,
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors( // hack to enable clickable
                    disabledTextColor = LocalContentColor.current.copy(LocalContentAlpha.current),
                    disabledBorderColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                    disabledLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                    disabledLeadingIconColor = MaterialTheme.colors.onSurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = true }),
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                repos.iterator().forEach { repo ->
                    DropdownMenuItem(onClick = {
                        onRepoChanged(repo)
                        expanded = false
                    }) {
                        RepoItem(repo, repo.repoId == preferredRepoId)
                    }
                }
            }
        }
        if (currentRepo.repoId != preferredRepoId) {
            FDroidOutlineButton(
                text = stringResource(R.string.app_details_repository_button_prefer),
                imageVector = Icons.Default.Star,
                onClick = { onPreferredRepoChanged(currentRepo.repoId) },
                modifier = Modifier.align(End),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
private fun RepoItem(repo: Repository, isPreferred: Boolean, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = CenterVertically,
        modifier = modifier,
    ) {
        val localeList = LocaleListCompat.getDefault()
        val res = LocalContext.current.resources
        if (LocalInspectionMode.current) Image(
            painter = rememberDrawablePainter(
                ResourcesCompat.getDrawable(res, R.drawable.ic_launcher, null)
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        ) else GlideImage(
            model = Utils.getDownloadRequest(repo, repo.getIcon(localeList)),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        ) {
            it.fallback(R.drawable.ic_repo_app_default).error(R.drawable.ic_repo_app_default)
        }
        Text(
            text = buildAnnotatedString {
                append(repo.getName(localeList) ?: "Unknown Repository")
                if (isPreferred) {
                    append(" ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append("★ ")
                    append(stringResource(R.string.app_details_repository_preferred))
                }
            },
            style = MaterialTheme.typography.body2,
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
fun RepoChooserSingleRepoPreview() {
    val repo1 = Repository(1L, "1", 1L, TWO, null, 1L, 1, 1L)
    FDroidContent {
        RepoChooser(listOf(repo1), 1L, 1L, {}, {})
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
fun RepoChooserPreview() {
    val repo1 = Repository(1L, "1", 1L, TWO, null, 1L, 1, 1L)
    val repo2 = Repository(2L, "2", 2L, TWO, null, 2L, 2, 2L)
    val repo3 = Repository(3L, "2", 3L, TWO, null, 3L, 3, 3L)
    FDroidContent {
        RepoChooser(listOf(repo1, repo2, repo3), 1L, 1L, {}, {})
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun RepoChooserNightPreview() {
    val repo1 = Repository(1L, "1", 1L, TWO, null, 1L, 1, 1L)
    val repo2 = Repository(2L, "2", 2L, TWO, null, 2L, 2, 2L)
    val repo3 = Repository(3L, "2", 3L, TWO, null, 3L, 3, 3L)
    FDroidContent {
        RepoChooser(listOf(repo1, repo2, repo3), 1L, 2L, {}, {})
    }
}
