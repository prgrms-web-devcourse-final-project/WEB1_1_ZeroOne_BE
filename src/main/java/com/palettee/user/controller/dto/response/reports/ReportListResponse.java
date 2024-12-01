package com.palettee.user.controller.dto.response.reports;

import com.palettee.archive.controller.dto.response.*;
import java.util.*;

public record ReportListResponse(
        List<SimpleReportResponse> reports,
        SliceInfo slice
) {

}
