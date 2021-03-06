package com.example.android_repo_04.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.android_repo_04.R
import com.example.android_repo_04.repository.GitHubApiRepository
import com.example.android_repo_04.databinding.ActivityMainBinding
import com.example.android_repo_04.utils.EventObserver
import com.example.android_repo_04.view.main.issue.IssueFragment
import com.example.android_repo_04.view.main.notification.NotificationFragment
import com.example.android_repo_04.view.profile.ProfileActivity
import com.example.android_repo_04.view.search.SearchActivity
import com.example.android_repo_04.viewmodel.CustomViewModelFactory
import com.example.android_repo_04.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = requireNotNull(_binding)

    private lateinit var viewModel: MainViewModel

    private val issueFragment: IssueFragment by lazy {
        IssueFragment()
    }

    private val notificationFragment: NotificationFragment by lazy {
        NotificationFragment()
    }

    private val fragmentManager: FragmentManager by lazy {
        supportFragmentManager
    }

    private val positionObserver: (Int) -> Unit = {
        when (it) {
            0 -> {
                selectIssue()
                showIssueFragment()
            }
            1 -> {
                selectNotification()
                showNotificationFragment()
            }
        }
    }

    private val clickEventObserver: (Int) -> Unit = { event ->
        when(event) {
            R.id.img_main_profile -> showProfileActivity()
            R.id.img_main_search -> showSearchActivity()
            R.id.btn_main_notification -> viewModel.position.value = 1
            R.id.btn_main_issue -> viewModel.position.value = 0
        }
    }

    /* onCreate */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initBinding()
        observeData()
        initFragmentManager()
        requestApi()
    }

    private fun requestApi() {
        getIssues()
        getNotifications()
        getUser()
        getUserStarred()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this,
            CustomViewModelFactory(GitHubApiRepository.getGitInstance()!!)
        )[MainViewModel::class.java]
    }

    private fun initBinding() {
        binding.vm = viewModel
        binding.lifecycleOwner = this
    }

    private fun observeData() {
        viewModel.position.observe(this, positionObserver)
        viewModel.mainClickEvent.observe(this, EventObserver(clickEventObserver))
    }

    private fun initFragmentManager() {
        if (fragmentManager.fragments.isEmpty()) {
            fragmentManager.commit {
                viewModel.position.value = 0
                add(R.id.layout_main_fragment_container, notificationFragment, getString(R.string.tag_notification_fragment))
                add(R.id.layout_main_fragment_container, issueFragment, getString(R.string.tag_issue_fragment))
            }
        }
    }

    private fun getIssues() {
        if (viewModel.issue.value == null)
            viewModel.requestIssues(getString(R.string.state_open))
    }

    private fun getNotifications() {
        if (viewModel.notifications.value == null)
            viewModel.requestNotifications()
    }

    private fun getUser() {
        viewModel.requestUser()
    }

    private fun getUserStarred() {
        viewModel.requestUserStarred()
    }

    private fun selectIssue(){
        binding.btnMainIssue.setBackgroundResource(R.drawable.btn_round_selected)
        binding.btnMainNotification.setBackgroundResource(R.drawable.btn_round_unselected)
    }

    private fun showIssueFragment(){
        fragmentManager.commit {
            show(fragmentManager.findFragmentByTag(getString(R.string.tag_issue_fragment))!!)
            hide(fragmentManager.findFragmentByTag(getString(R.string.tag_notification_fragment))!!)
        }
    }

    private fun selectNotification(){
        binding.btnMainIssue.setBackgroundResource(R.drawable.btn_round_unselected)
        binding.btnMainNotification.setBackgroundResource(R.drawable.btn_round_selected)
    }

    private fun showNotificationFragment(){
        fragmentManager.commit {
            show(fragmentManager.findFragmentByTag(getString(R.string.tag_notification_fragment))!!)
            hide(fragmentManager.findFragmentByTag(getString(R.string.tag_issue_fragment))!!)
        }
    }

    private fun showProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(getString(R.string.user_info), viewModel.user.value)
        intent.putExtra(getString(R.string.star_count), viewModel.starCount.value)
        startActivity(intent)
    }

    private fun showSearchActivity() {
        startActivity(Intent(this, SearchActivity::class.java))
    }
}