package lm.pkp.com.landmap.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.drive.DriveDBHelper;
import lm.pkp.com.landmap.drive.DriveResource;

/**
 * Created by USER on 11/2/2017.
 */
public class AreaPopulationUtil {

    public static final AreaPopulationUtil INSTANCE = new AreaPopulationUtil();

    private AreaPopulationUtil() {
    }

    public void populateAreaElement(View view) {
        final AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        populateAreaElement(view, areaElement);
    }

    public void populateAreaElement(View view, final AreaElement ae) {

        TextView areaNameView = (TextView) view.findViewById(R.id.area_name_text);
        String areaName = ae.getName();
        if (areaName.length() > 25) {
            areaNameView.setText(areaName.substring(0, 22).concat("..."));
        } else {
            areaNameView.setText(areaName);
        }

        TextView descText = (TextView) view.findViewById(R.id.area_desc_text);
        String desc = ae.getDescription();
        desc = "<b>Description: </b>" + desc;
        descText.setText(Html.fromHtml(desc));

        TextView creatorText = (TextView) view.findViewById(R.id.area_creator_text);
        creatorText.setText(Html.fromHtml("<b>Creator: </b>" + ae.getCreatedBy()));

        TextView tagsText = (TextView) view.findViewById(R.id.area_tags_text);
        String areaTags = ae.getAddress();
        String tagsContent = "<b>Address: </b>" + areaTags;
        tagsText.setText(Html.fromHtml(tagsContent));

        double areaMeasureSqFt = ae.getMeasureSqFt();
        double areaMeasureAcre = areaMeasureSqFt / 43560;
        double areaMeasureDecimals = areaMeasureSqFt / 436;
        DecimalFormat df = new DecimalFormat("###.##");

        TextView measureText = (TextView) view.findViewById(R.id.area_measure_text);
        String content = "<b>Area: </b>" + df.format(areaMeasureSqFt) + " Sqft, "
                + df.format(areaMeasureAcre) + " Acre, " + df.format(areaMeasureDecimals) + " Decimals.";
        measureText.setText(Html.fromHtml(content));

        final Drawable drawable = view.getBackground().getCurrent();
        if (drawable instanceof GradientDrawable) {
            ((GradientDrawable) drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        } else if (drawable instanceof ColorDrawable) {
            ((ColorDrawable) drawable).setColor(ColorProvider.getAreaDetailsColor(ae));
        }

        DriveDBHelper ddh = new DriveDBHelper(view.getContext());
        List<DriveResource> imageResources = ddh.fetchImageResources(ae);
        ImageView areaImg = (ImageView) view.findViewById(R.id.area_default_img);

        Iterator<DriveResource> imageResIter = imageResources.iterator();
        String thumbRootPath = AreaContext.INSTANCE
                .getAreaLocalPictureThumbnailRoot(ae.getUniqueId()).getAbsolutePath();

        while (imageResIter.hasNext()) {
            final DriveResource imageResource = imageResIter.next();
            final String imageName = imageResource.getName();
            String thumbnailPath = thumbRootPath + File.separatorChar + imageName;
            File thumbFile = new File(thumbnailPath);
            if(thumbFile.exists()){
                Bitmap bMap = BitmapFactory.decodeFile(thumbnailPath);
                areaImg.setImageBitmap(bMap);
            }
            break;
        }
    }

}
