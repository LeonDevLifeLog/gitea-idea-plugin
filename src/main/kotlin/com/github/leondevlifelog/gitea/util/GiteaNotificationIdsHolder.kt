/*
 * Copyright (c) 2023. Leon<leondevlifelog@gmail.com>. All rights reserved.
 * SPDX-License-Identifier: MIT
 */

package com.github.leondevlifelog.gitea.util


class GiteaNotificationIdsHolder {
    fun getNotificationIds(): List<String> {
        return listOf(
            MISSING_DEFAULT_ACCOUNT,
            PULL_REQUEST_CANNOT_SET_TRACKING_BRANCH,
            CLONE_UNABLE_TO_CREATE_DESTINATION_DIR,
            CLONE_UNABLE_TO_FIND_DESTINATION,
            OPEN_IN_BROWSER_FILE_IS_NOT_UNDER_REPO,
            OPEN_IN_BROWSER_CANNOT_GET_LAST_REVISION,
            REBASE_SUCCESS,
            GIST_CANNOT_CREATE,
            PULL_REQUEST_CANNOT_LOAD_BRANCHES,
            PULL_REQUEST_CANNOT_COLLECT_ADDITIONAL_DATA,
            PULL_REQUEST_CANNOT_LOAD_FORKS,
            PULL_REQUEST_FAILED_TO_ADD_REMOTE,
            PULL_REQUEST_PUSH_FAILED,
            PULL_REQUEST_CREATION_ERROR,
            PULL_REQUEST_CANNOT_COLLECT_DIFF_DATA,
            PULL_REQUEST_CANNOT_FIND_REPO,
            PULL_REQUEST_CREATED,
            PULL_REQUEST_CANNOT_PROCESS_REMOTE,
            PULL_REQUEST_NO_CURRENT_BRANCH,
            REBASE_CANNOT_VALIDATE_UPSTREAM_REMOTE,
            REBASE_UPSTREAM_IS_OWN_REPO,
            REBASE_CANNOT_GER_USER_INFO,
            REBASE_CANNOT_RETRIEVE_UPSTREAM_INFO,
            REBASE_CANNOT_CONFIGURE_UPSTREAM_REMOTE,
            REBASE_REPO_NOT_FOUND,
            REBASE_CANNOT_LOAD_REPO_INFO,
            REBASE_REPO_IS_NOT_A_FORK,
            REBASE_MULTI_REPO_NOT_SUPPORTED,
            REBASE_REMOTE_ORIGIN_NOT_FOUND,
            REBASE_ACCOUNT_NOT_FOUND,
            REBASE_FAILED_TO_MATCH_REPO,
            SHARE_CANNOT_FIND_GIT_REPO,
            SHARE_CANNOT_CREATE_REPO,
            SHARE_PROJECT_SUCCESSFULLY_SHARED,
            SHARE_EMPTY_REPO_CREATED,
            SHARE_PROJECT_INIT_COMMIT_FAILED,
            SHARE_PROJECT_INIT_PUSH_FAILED,
            GIST_CREATED,
            GIT_REPO_INIT_REPO
        )
    }

    companion object {
        const val MISSING_DEFAULT_ACCOUNT = "gitea.missing.default.account"
        const val PULL_REQUEST_CANNOT_SET_TRACKING_BRANCH = "gitea.pull.request.cannot.set.tracking.branch"
        const val CLONE_UNABLE_TO_CREATE_DESTINATION_DIR = "gitea.clone.unable.to.create.destination.dir"
        const val CLONE_UNABLE_TO_FIND_DESTINATION = "gitea.clone.unable.to.find.destination"
        const val OPEN_IN_BROWSER_FILE_IS_NOT_UNDER_REPO = "gitea.open.in.browser.file.is.not.under.repo"
        const val OPEN_IN_BROWSER_CANNOT_GET_LAST_REVISION = "gitea.open.in.browser.cannot.get.last.revision"
        const val REBASE_SUCCESS = "gitea.rebase.success"
        const val GIST_CANNOT_CREATE = "gitea.gist.cannot.create"
        const val PULL_REQUEST_CANNOT_LOAD_BRANCHES = "gitea.pull.request.cannot.load.branches"
        const val PULL_REQUEST_CANNOT_COLLECT_ADDITIONAL_DATA = "gitea.pull.request.cannot.collect.additional.data"
        const val PULL_REQUEST_CANNOT_LOAD_FORKS = "gitea.pull.request.cannot.load.forks"
        const val PULL_REQUEST_FAILED_TO_ADD_REMOTE = "gitea.pull.request.failed.to.add.remote"
        const val PULL_REQUEST_PUSH_FAILED = "gitea.pull.request.push.failed"
        const val PULL_REQUEST_CREATION_ERROR = "gitea.pull.request.creation.error"
        const val PULL_REQUEST_CANNOT_COLLECT_DIFF_DATA = "gitea.pull.request.cannot.collect.diff.data"
        const val PULL_REQUEST_CANNOT_FIND_REPO = "gitea.pull.request.cannot.find.repo"
        const val PULL_REQUEST_CREATED = "gitea.pull.request.created"
        const val PULL_REQUEST_CANNOT_PROCESS_REMOTE = "gitea.pull.request.cannot.process.remote"
        const val PULL_REQUEST_NO_CURRENT_BRANCH = "gitea.pull.request.no.current.branch"
        const val REBASE_CANNOT_VALIDATE_UPSTREAM_REMOTE = "gitea.rebase.cannot.validate.upstream.remote"
        const val REBASE_UPSTREAM_IS_OWN_REPO = "gitea.rebase.upstream.is.own.repo"
        const val REBASE_CANNOT_GER_USER_INFO = "gitea.rebase.cannot.get.user.info"
        const val REBASE_CANNOT_RETRIEVE_UPSTREAM_INFO = "gitea.rebase.cannot.retrieve.upstream.info"
        const val REBASE_CANNOT_CONFIGURE_UPSTREAM_REMOTE = "gitea.rebase.cannot.configure.upstream.remote"
        const val REBASE_REPO_NOT_FOUND = "gitea.rebase.repo.not.found"
        const val REBASE_CANNOT_LOAD_REPO_INFO = "gitea.rebase.cannot.load.repo.info"
        const val REBASE_REPO_IS_NOT_A_FORK = "gitea.rebase.repo.is.not.a.fork"
        const val REBASE_MULTI_REPO_NOT_SUPPORTED = "gitea.rebase.multi.repo.not.supported"
        const val REBASE_REMOTE_ORIGIN_NOT_FOUND = "gitea.rebase.remote.origin.not.found"
        const val REBASE_ACCOUNT_NOT_FOUND = "gitea.rebase.account.not.found"
        const val REBASE_FAILED_TO_MATCH_REPO = "rebase.error.failed.to.match.gh.repo"
        const val SHARE_CANNOT_FIND_GIT_REPO = "gitea.share.cannot.find.git.repo"
        const val SHARE_CANNOT_CREATE_REPO = "gitea.share.cannot.create.repo"
        const val SHARE_PROJECT_SUCCESSFULLY_SHARED = "gitea.share.project.successfully.shared"
        const val SHARE_EMPTY_REPO_CREATED = "gitea.share.empty.repo.created"
        const val SHARE_PROJECT_INIT_COMMIT_FAILED = "gitea.share.project.created.init.commit.failed"
        const val SHARE_PROJECT_INIT_PUSH_FAILED = "gitea.share.init.push.failed"
        const val GIST_CREATED = "gitea.gist.created"
        const val GIT_REPO_INIT_REPO = "gitea.git.repo.init.error"
    }
}