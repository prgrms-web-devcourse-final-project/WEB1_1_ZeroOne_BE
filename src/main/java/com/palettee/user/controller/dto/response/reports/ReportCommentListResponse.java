package com.palettee.user.controller.dto.response.reports;

import com.palettee.archive.controller.dto.response.*;
import java.util.*;

public record ReportCommentListResponse(
        List<SimpleReportCommentResponse> comments,
        SliceInfo slice
) {

}
