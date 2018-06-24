package org.fossasia.openevent.general.ticket

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_tickets.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.event.Event
import org.fossasia.openevent.general.event.EventUtils
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import java.lang.StringBuilder

class TicketsFragment : Fragment() {
    private val ticketsRecyclerAdapter: TicketsRecyclerAdapter = TicketsRecyclerAdapter()
    private val ticketsViewModel by viewModel<TicketsViewModel>()
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private lateinit var rootView: View
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_tickets, container, false)
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Ticket Details"
        setHasOptionsMenu(true)

        rootView.ticketsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.ticketsRecycler.adapter = ticketsRecyclerAdapter
        rootView.ticketsRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.ticketsRecycler.layoutManager = linearLayoutManager

        ticketsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        ticketsViewModel.progressTickets.observe(this, Observer {
            it?.let { Utils.showProgressBar(rootView.progressBarTicket, it) }
        })

        ticketsViewModel.event.observe(this, Observer {
            it?.let { loadEventDetails(it) }
        })

        ticketsViewModel.loadEvent(id)
        ticketsViewModel.loadTickets(id)

        ticketsViewModel.tickets.observe(this, Observer {
            it?.let {
                ticketsRecyclerAdapter.addAll(it)
            }
            ticketsRecyclerAdapter.notifyDataSetChanged()
        })

        return rootView
    }

    override fun onDestroyView() {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadEventDetails(event: Event) {
        rootView.eventName.text = event.name
        rootView.organizerName.text = "by ${event.organizerName.nullToEmpty()}"
        val dateString = StringBuilder()
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.time.text = dateString.append(EventUtils.getFormattedDate(startsAt))
                .append(" - ")
                .append(EventUtils.getFormattedDate(endsAt))
                .append(" • ")
                .append(EventUtils.getFormattedTime(startsAt))
    }

}