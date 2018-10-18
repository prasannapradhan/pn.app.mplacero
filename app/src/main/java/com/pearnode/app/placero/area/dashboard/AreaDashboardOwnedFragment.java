package com.pearnode.app.placero.area.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.pearnode.app.placero.AreaDetailsActivity;
import com.pearnode.app.placero.R;
import com.pearnode.app.placero.R.id;
import com.pearnode.app.placero.R.layout;
import com.pearnode.app.placero.area.AreaContext;
import com.pearnode.app.placero.area.AreaDashboardDisplayMetaStore;
import com.pearnode.app.placero.area.db.AreaDBHelper;
import com.pearnode.app.placero.area.model.Area;
import com.pearnode.app.placero.area.res.disp.AreaItemAdaptor;
import com.pearnode.app.placero.area.tasks.CreateAreaTask;
import com.pearnode.app.placero.custom.FragmentFilterHandler;
import com.pearnode.app.placero.custom.FragmentHandler;
import com.pearnode.app.placero.permission.PermissionConstants;
import com.pearnode.app.placero.permission.PermissionElement;
import com.pearnode.app.placero.permission.PermissionsDBHelper;
import com.pearnode.app.placero.tags.TagElement;
import com.pearnode.app.placero.user.UserContext;
import com.pearnode.app.placero.user.UserElement;
import com.pearnode.app.placero.user.UserPersistableSelections;
import com.pearnode.common.TaskFinishedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by USER on 11/4/2017.
 */
public class AreaDashboardOwnedFragment extends Fragment implements FragmentFilterHandler, FragmentHandler{

    private Activity activity = null;
    private View mView = null;
    private AreaItemAdaptor viewAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owned_areas, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        activity = getActivity();
        if(getUserVisibleHint()){
            loadFragment();
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && (mView != null) && (activity != null)) {
            AreaDashboardDisplayMetaStore.INSTANCE.setActiveTab(AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ);
            loadFragment();
        }
    }

    private void loadFragment() {
        AreaContext.INSTANCE.clearContext();
        mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

        ArrayList<Area> areas = new AreaDBHelper(activity).getAreas("self");
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);

        ImageView createAreaView = (ImageView) mView.findViewById(id.owned_area_empty_layout_action);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

                Area area = new Area();
                String uniqueId = UUID.randomUUID().toString();
                area.setUniqueId(uniqueId);
                area.setName("PL_" + uniqueId);
                area.setCreatedBy(UserContext.getInstance().getUserElement().getEmail());

                PermissionElement pe = new PermissionElement();
                pe.setUserId(UserContext.getInstance().getUserElement().getEmail());
                pe.setAreaId(area.getUniqueId());
                pe.setFunctionCode(PermissionConstants.FULL_CONTROL);

                PermissionsDBHelper pdh = new PermissionsDBHelper(activity);
                pdh.insertPermissionLocally(pe);
                area.getUserPermissions().put(PermissionConstants.FULL_CONTROL, pe);

                // Resetting the context for new Area
                AreaContext.INSTANCE.setAreaElement(area, activity);
                CreateAreaServerCallback createCallback = new CreateAreaServerCallback();
                createCallback.setArea(area);

                CreateAreaTask createAreaTask = new CreateAreaTask(activity, createCallback);
                createAreaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, area);
            }
        });

        if (areas.size() > 0) {
            mView.findViewById(R.id.owned_area_empty_layout).setVisibility(View.GONE);
            areaListView.setVisibility(View.VISIBLE);

            viewAdapter = new AreaItemAdaptor(activity, layout.area_element_row, areas);
            areaListView.setAdapter(viewAdapter);
            areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        } else {
            areaListView.setVisibility(View.GONE);
            mView.findViewById(id.owned_area_empty_layout).setVisibility(View.VISIBLE);
        }

        EditText inputSearch = (EditText) activity.findViewById(id.dashboard_search_box);
        if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
            inputSearch.addTextChangedListener(new UserInputWatcher());
        }

        Button seachClearButton = (Button) activity.findViewById(id.dashboard_search_clear);
        if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
                seachClearButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText inputSearch = (EditText) activity.findViewById(id.dashboard_search_box);
                        inputSearch.setText("");
                    }
                });
        }
        mView.findViewById(id.splash_panel).setVisibility(View.GONE);

        final ImageView filterUTView = (ImageView) activity.findViewById(id.action_filter_ut);
        UserElement userElement = UserContext.getInstance().getUserElement();
        UserPersistableSelections userPersistableSelections = userElement.getSelections();
        if(userPersistableSelections.isFilter()){
            filterUTView.setBackground(getResources().getDrawable(R.drawable.rounded_corner));
            List<TagElement> tags = userPersistableSelections.getTags();
            List<String> filterables = new ArrayList<>();
            List<String> executables = new ArrayList<>();
            for(TagElement tag: tags){
                if(tag.getType().equals("filterable")){
                    filterables.add(tag.getName());
                }else {
                    executables.add(tag.getName());
                }
            }
            doFilter(filterables, executables);
        }else {
            filterUTView.setBackground(null);
        }
    }

    private class CreateAreaServerCallback implements TaskFinishedListener {

        private Area area = null;

        public Area getArea() {
            return this.area;
        }

        public void setArea(Area area) {
            this.area = area;
        }

        @Override
        public void onTaskFinished(String response) {
            Intent intent = new Intent(activity, AreaDetailsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public String getFragmentTitle() {
        return "Owned";
    }

    @Override
    public void doFilter(List<String> filterables, List<String> executables) {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        AreaItemAdaptor adapter = (AreaItemAdaptor) areaListView.getAdapter();
        if(adapter.getCount() == 0){
            return;
        }
        EditText inputSearch = (EditText) activity.findViewById(id.dashboard_search_box);
        Editable inputSearchText = inputSearch.getText();
        adapter.getFilterChain(filterables, executables).filter(inputSearchText.toString());
    }

    @Override
    public void resetFilter() {
        ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
        AreaItemAdaptor adapter = (AreaItemAdaptor) areaListView.getAdapter();
        if(adapter == null){
            return;
        }
        adapter.resetFilter().filter(null);
    }

    @Override
    public Object getViewAdaptor() {
        return viewAdapter;
    }

    private class UserInputWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(AreaDashboardDisplayMetaStore.INSTANCE.getActiveTab() == AreaDashboardDisplayMetaStore.TAB_OWNED_SEQ){
                if(editable.toString().equalsIgnoreCase("")){
                    return;
                }else {
                    mView.findViewById(id.splash_panel).setVisibility(View.VISIBLE);

                    ListView areaListView = (ListView) mView.findViewById(id.area_display_list);
                    ArrayAdapter<Area> adapter = (ArrayAdapter<Area>) areaListView.getAdapter();
                    final ImageView filterUTView = (ImageView) activity.findViewById(id.action_filter_ut);
                    UserElement userElement = UserContext.getInstance().getUserElement();
                    if(filterUTView.getBackground() != null){
                        List<TagElement> tags = userElement.getSelections().getTags();
                        List<String> filterables = new ArrayList<>();
                        List<String> executables = new ArrayList<>();
                        for(TagElement tag: tags){
                            if(tag.getType().equals("filterable")){
                                filterables.add(tag.getName());
                            }else {
                                executables.add(tag.getName());
                            }
                        }
                        doFilter(filterables, executables);
                    }else {
                        adapter.getFilter().filter(editable.toString());
                    }
                    mView.findViewById(id.splash_panel).setVisibility(View.GONE);
                }
            }
        }
    }

}