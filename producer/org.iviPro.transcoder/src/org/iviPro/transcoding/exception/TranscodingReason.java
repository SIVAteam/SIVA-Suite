package org.iviPro.transcoding.exception;

public class TranscodingReason implements Reason {

	private final TranscodingReasonDescriptor reasonDescriptor;
	private final String[] details;

	public TranscodingReason(TranscodingReasonDescriptor reasonDescriptor,
			String... details) {
		this.reasonDescriptor = reasonDescriptor;
		this.details = details;
	}

	@Override
	public int getErrorCode() {
		return reasonDescriptor.getErrorCode();
	}

	@Override
	public String getIdentifier() {
		return reasonDescriptor.getIdentifier();
	}

	@Override
	public String getMessage() {
		return String.format(reasonDescriptor.getMessage(), (Object[]) details);
	}

	@Override
	public String[] getDetails() {
		return details;
	}

}
