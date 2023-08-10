package com.github.leondevlifelog.gitea.tasks

import com.intellij.tasks.generic.GenericTask
import org.gitnex.tea4j.v2.models.Issue

class GiteaTask(private val giteaRepository: GiteaRepository, private val issue: Issue) :
    GenericTask(issue.id.toString(), issue.title, giteaRepository) {}