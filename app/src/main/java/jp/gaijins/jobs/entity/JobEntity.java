package jp.gaijins.jobs.entity;

import android.os.Parcel;
import android.os.Parcelable;

import proguard.annotation.KeepClassMemberNames;

/**
 * Created by nayak.vishal on 2015/12/14.
 */

@KeepClassMemberNames
public class JobEntity implements Parcelable {
    private String mJobType;
    private String mJobUrl;
    private String mImageUrl;
    private String mJobTitle;
    private String mJobDomainName;
    private String mJobDomainThumbnailUrl;

    public JobEntity(String jobType, String jobUrl, String imageUrl, String jobTitle, String jobDomainName, String jobDomainThumbnailUrl) {
        this.mJobType = jobType;
        this.mJobUrl = jobUrl;
        this.mImageUrl = imageUrl;
        this.mJobTitle = jobTitle;
        this.mJobDomainName = jobDomainName;
        this.mJobDomainThumbnailUrl = jobDomainThumbnailUrl;
    }

    public JobEntity(Parcel in) {
        mJobType = in.readString();
        mJobUrl = in.readString();
        mImageUrl = in.readString();
        mJobTitle = in.readString();
        mJobDomainName = in.readString();
        mJobDomainThumbnailUrl = in.readString();
    }

    public String getJobUrl() {
        return mJobUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getJobTitle() {
        return mJobTitle;
    }

    public String getJobDomainName() {
        return mJobDomainName;
    }

    public String getJobDomainThumbnailUrl() {
        return mJobDomainThumbnailUrl;
    }

    public static final Creator<JobEntity> CREATOR = new Creator<JobEntity>() {
        @Override
        public JobEntity[] newArray(int size) {
            return new JobEntity[size];
        }

        @Override
        public JobEntity createFromParcel(Parcel source) {
            return new JobEntity(source);
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mJobType);
        dest.writeString(mJobUrl);
        dest.writeString(mImageUrl);
        dest.writeString(mJobTitle);
        dest.writeString(mJobDomainName);
        dest.writeString(mJobDomainThumbnailUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
